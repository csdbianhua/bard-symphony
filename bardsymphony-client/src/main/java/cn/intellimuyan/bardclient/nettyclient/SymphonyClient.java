package cn.intellimuyan.bardclient.nettyclient;

import cn.intellimuyan.bardclient.model.BardCommand;
import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.nettyclient.inbound.BardCommandDecoder;
import cn.intellimuyan.bardclient.nettyclient.inbound.HeartbeatHandler;
import cn.intellimuyan.bardclient.nettyclient.outbound.BardCommandEncoder;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * 乐团客户端
 */
@Service
@Profile("!debug")
@Slf4j
public class SymphonyClient implements ISymphonyClient {
    @Value("${remote.host:127.0.0.1}")
    private String host;
    @Value("${remote.port:9999}")
    private Integer port;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private Bootstrap b;
    private final List<ChannelInboundHandlerAdapter> inboundHandlers;
    private final List<ChannelOutboundHandlerAdapter> outboundHandlers;


    public SymphonyClient(ObjectProvider<ChannelInboundHandlerAdapter> inboundHandlers,
                          ObjectProvider<ChannelOutboundHandlerAdapter> outboundHandlers) {
        this.inboundHandlers = inboundHandlers.orderedStream().collect(toList());
        this.outboundHandlers = outboundHandlers.orderedStream().collect(toList());
    }

    @Override
    public synchronized void connect() {
        if (channel != null) {
            return;
        }
        this.workerGroup = new NioEventLoopGroup();
        this.b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();
                p.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                p.addLast(new HeartbeatHandler());
                p.addLast(new LengthFieldBasedFrameDecoder(64 * 1024, 0, Integer.BYTES));
                p.addLast(new BardCommandDecoder());
                for (ChannelInboundHandlerAdapter handler : inboundHandlers) {
                    p.addLast(handler);
                }
                for (ChannelOutboundHandlerAdapter handler : outboundHandlers) {
                    p.addLast(handler);
                }
                p.addLast(new BardCommandEncoder());
            }
        });
        channel = b.connect(host, port).syncUninterruptibly().channel();
    }


    @PreDestroy
    @SneakyThrows
    @Override
    public void close() {
        if (channel == null || !channel.isOpen()) {
            return;
        }
        BardCommand cmd = new BardCommand();
        cmd.setCmd(CmdType.LEAVE);
        channel.writeAndFlush(cmd).syncUninterruptibly();
        if (workerGroup != null) {
            workerGroup.shutdownGracefully().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        channel = null;
    }

    @Override
    public boolean isOnline() {
        return channel != null;
    }

    @Override
    public void sendCmd(CmdType cmdType, Object payload) {
        if (channel == null) {
            log.warn("[交响乐客户端]没有链接,命令丢失,{} - {}", cmdType, payload);
            return;
        }
        BardCommand cmd = new BardCommand();
        cmd.setCmd(cmdType);
        cmd.setPayload(payload == null ? null : JSON.toJSONString(payload));
        channel.writeAndFlush(cmd);
    }

}

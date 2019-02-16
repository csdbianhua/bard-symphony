package cn.intellimuyan.bardsymphony.nettyserver;

import cn.intellimuyan.bardsymphony.nettyserver.inbound.BardCommandDecoder;
import cn.intellimuyan.bardsymphony.nettyserver.inbound.HeartbeatHandler;
import cn.intellimuyan.bardsymphony.nettyserver.outbound.BardCommandEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

/**
 * @author hason
 * @version 19-1-28
 */
@Service
@Slf4j
public class SymphonyServer {

    @Value("${port:9999}")
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final List<ChannelInboundHandlerAdapter> inboundHandlers;
    private final List<ChannelOutboundHandlerAdapter> outboundHandlers;

    public SymphonyServer(ObjectProvider<ChannelInboundHandlerAdapter> inboundHandlers,
                          ObjectProvider<ChannelOutboundHandlerAdapter> outboundHandlers) {
        this.inboundHandlers = inboundHandlers.orderedStream().collect(toList());
        this.outboundHandlers = outboundHandlers.orderedStream().collect(toList());
    }


    @PostConstruct
    public void run() throws Exception {
        String os = System.getProperty("os.name");
        boolean isUnix = os.contains("nix") || os.contains("nux") || os.contains("aix");
        if (isUnix) {
            log.info("[NioServer]detected *nix operating system, use epoll : {}", os);
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
        } else {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        }
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(isUnix ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
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
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        b.bind(port).addListener(future -> log.info("[NioServer]started on port : {}", port));
    }

    @PreDestroy
    public void close() {
        for (Future<?> future : Arrays.asList(workerGroup.shutdownGracefully(), bossGroup.shutdownGracefully())) {
            try {
                future.syncUninterruptibly();
            } catch (RuntimeException e) {
                log.error("[NioServer]closed failed", e);
            }
        }
        log.info("[NioServer]closed success");

    }
}


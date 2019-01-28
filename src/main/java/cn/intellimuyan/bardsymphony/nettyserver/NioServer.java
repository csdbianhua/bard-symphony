package cn.intellimuyan.bardsymphony.nettyserver;

import cn.intellimuyan.bardsymphony.nettyserver.inbound.BardCommandDecoder;
import cn.intellimuyan.bardsymphony.nettyserver.outbound.BardCommandEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.stream.Collectors.toList;

/**
 * @author hason
 * @version 19-1-28
 */
@Service
@Slf4j
public class NioServer {

    @Value("${port:9999}")
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final List<ChannelInboundHandlerAdapter> inboundHandlers;
    private final List<ChannelOutboundHandlerAdapter> outboundHandlers;

    public NioServer(ObjectProvider<ChannelInboundHandlerAdapter> inboundHandlers,
                     ObjectProvider<ChannelOutboundHandlerAdapter> outboundHandlers) {
        this.inboundHandlers = inboundHandlers.orderedStream().collect(toList());
        this.outboundHandlers = outboundHandlers.orderedStream().collect(toList());
    }


    @PostConstruct
    public void run() throws Exception {
        bossGroup = new NioEventLoopGroup(); // (1)
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap(); // (2)
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new BardCommandDecoder());
                        for (ChannelInboundHandlerAdapter handler : inboundHandlers) {
                            pipeline.addLast(handler);
                        }
                        for (ChannelOutboundHandlerAdapter handler : outboundHandlers) {
                            pipeline.addLast(handler);
                        }
                        pipeline.addLast(new BardCommandEncoder());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
        b.bind(port).addListener(future -> log.info("[NioServer]started on port : {}", port));
    }

    @PreDestroy
    public void close() {
        for (Future<?> future : Arrays.asList(workerGroup.shutdownGracefully(), bossGroup.shutdownGracefully())) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("[NioServer]closed failed", e);
            }
        }
        log.info("[NioServer]closed success");

    }
}


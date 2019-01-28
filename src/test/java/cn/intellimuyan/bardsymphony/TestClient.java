package cn.intellimuyan.bardsymphony;

import cn.intellimuyan.bardsymphony.nettyserver.inbound.BardCommandDecoder;
import cn.intellimuyan.bardsymphony.nettyserver.model.InstrumentEnum;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.PlayMsg;
import cn.intellimuyan.bardsymphony.nettyserver.outbound.BardCommandEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author hason
 * @version 19-1-28
 */
public class TestClient {
    public static void main(String[] args) throws InterruptedException, IOException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new BardCommandDecoder()).addLast(
                            new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println(msg);
                                }
                            }
                    ).addLast(new BardCommandEncoder());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("localhost", 9999).sync(); // (5)
            Channel ctx = f.channel();
            JoinMsg joinMsg = new JoinMsg();
            joinMsg.setName("Jay");
            joinMsg.setInstrument(InstrumentEnum.PIANO.name());
            ctx.writeAndFlush(joinMsg);
            List<String> lines = Files.readAllLines(Paths.get(new ClassPathResource("music.bsy").getURI()));
            for (int i = 0; i < lines.size(); i++) {
                String note = lines.get(i);
                String nextNote;
                if ((i + 1) >= lines.size()) {
                    nextNote = "null";
                } else {
                    nextNote = lines.get(i + 1);
                }
                PlayMsg msg = new PlayMsg();
                msg.setNote(note);
                msg.setInstrument(InstrumentEnum.PIANO.name());
                ctx.write(msg);
            }
            ctx.flush();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

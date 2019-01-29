package cn.intellimuyan.bardsymphony;

import cn.intellimuyan.bardsymphony.nettyserver.inbound.BardCommandDecoder;
import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.InstrumentEnum;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.DirectMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.nettyserver.outbound.BardCommandEncoder;
import com.alibaba.fastjson.JSON;
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
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new BardCommandDecoder()).addLast(
                            new ChannelInboundHandlerAdapter() {

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    System.out.println("弹奏:" + msg);
                                }
                            }
                    ).addLast(new BardCommandEncoder());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("localhost", 9999).sync(); // (5)
            Thread.sleep(3000);
            Channel ctx = f.channel();
            BardCommand cmd = new BardCommand();
            cmd.setCmd(CmdType.JOIN);
            JoinMsg joinMsg = new JoinMsg();
            joinMsg.setName("Jay");
            joinMsg.setInstrument(InstrumentEnum.PIANO.name());
            cmd.setPayload(JSON.toJSONString(joinMsg));
            ctx.writeAndFlush(cmd);
            Thread.sleep(3000);
            List<String> lines = Files.readAllLines(Paths.get(new ClassPathResource("music.bsy").getURI()));
            for (String note : lines) {
                DirectMsg msg = new DirectMsg();
                msg.setNote(note);
                msg.setInstrument(InstrumentEnum.PIANO.name());
                BardCommand playCmd = new BardCommand();
                playCmd.setCmd(CmdType.DIRECT);
                playCmd.setPayload(JSON.toJSONString(msg));
                ctx.writeAndFlush(playCmd);
            }

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

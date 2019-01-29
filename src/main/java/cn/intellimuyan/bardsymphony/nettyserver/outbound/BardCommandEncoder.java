package cn.intellimuyan.bardsymphony.nettyserver.outbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * @author hason
 * @version 19-1-28
 */
@ChannelHandler.Sharable
public class BardCommandEncoder extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        BardCommand datum = (BardCommand) msg;
        String payloadStr = datum.getPayload();
        ByteBuf byteBuf;
        byte[] payload = payloadStr == null ? new byte[0] : payloadStr.getBytes();
        byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(Integer.BYTES * 2 + payload.length);
        byteBuf.writeInt(payload.length);
        byteBuf.writeInt(datum.getCmd().getCode());
        byteBuf.writeBytes(payload);
        ctx.writeAndFlush(byteBuf);
    }
}

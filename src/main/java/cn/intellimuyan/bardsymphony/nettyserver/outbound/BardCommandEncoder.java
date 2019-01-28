package cn.intellimuyan.bardsymphony.nettyserver.outbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.CmdEnum;
import com.alibaba.fastjson.JSON;
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
        Class<?> clz = msg.getClass();
        CmdEnum cmd = CmdEnum.fromClz(clz);
        ByteBuf byteBuf;
        byte[] payload = JSON.toJSONBytes(msg);
        byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(Integer.BYTES * 2 + payload.length);
        byteBuf.writeInt(payload.length);
        byteBuf.writeInt(cmd.getCode());
        byteBuf.writeBytes(payload);
        ctx.writeAndFlush(byteBuf);
    }
}

package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hason
 * @version 19-1-28
 */
@Slf4j
public class BardCommandDecoder extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        in.skipBytes(Integer.BYTES);
        int cmd = in.readInt();
        CmdType c = CmdType.fromCode(cmd);
        BardCommand msg = new BardCommand();
        msg.setCmd(c);
        int length = in.readableBytes();
        if (length != 0) {
            byte[] content = new byte[length];
            in.readBytes(content);
            msg.setPayload(new String(content));
        }
        if (log.isDebugEnabled()) {
            log.debug("[输入数据]{}", msg);
        }
        ctx.fireChannelRead(msg);
    }

}

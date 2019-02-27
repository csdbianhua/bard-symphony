package cn.intellimuyan.bardsymphony.nettyserver.outbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hason
 * @version 19-1-28
 */
@ChannelHandler.Sharable
@Slf4j
public class BardCommandEncoder extends MessageToByteEncoder<BardCommand> {

    @Override
    protected void encode(ChannelHandlerContext ctx, BardCommand msg, ByteBuf out) throws Exception {
        String payloadStr = msg.getPayload();
        byte[] payload = payloadStr == null ? new byte[0] : payloadStr.getBytes();
        out.writeInt(Integer.BYTES + payload.length);
        out.writeInt(msg.getCmd().getCode());
        out.writeBytes(payload);
        if (log.isDebugEnabled()) {
            log.debug("[输出数据]{}", msg);
        }
    }
}

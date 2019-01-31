package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author hason
 * @version 19-1-28
 */
@Slf4j
public class BardCommandDecoder extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        int cmd = in.readInt();
        CmdType c = CmdType.fromCode(cmd);
        if (c == null || length > 1024 * 1024) {
            if (log.isDebugEnabled()) {
                log.debug("[解码器]非法请求,cmd:{},length:{}", cmd, length);
            }
            ctx.close();
            return;
        }
        byte[] content = new byte[length];
        in.readBytes(content);
        BardCommand datum = new BardCommand();
        datum.setCmd(c);
        datum.setPayload(new String(content));
        out.add(datum);
        if (log.isDebugEnabled()) {
            log.debug("[输入数据]{}", datum);
        }
    }


}

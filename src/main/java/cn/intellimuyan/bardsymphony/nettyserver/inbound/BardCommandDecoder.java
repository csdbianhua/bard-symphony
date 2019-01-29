package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommandDatum;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author hason
 * @version 19-1-28
 */
public class BardCommandDecoder extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        int cmd = in.readInt();
        byte[] content = new byte[length];
        in.readBytes(content);
        BardCommandDatum datum = new BardCommandDatum();
        CmdType c = CmdType.fromCode(cmd);
        datum.setCmd(c);
        datum.setPayload(new String(content));
        out.add(datum);
    }


}

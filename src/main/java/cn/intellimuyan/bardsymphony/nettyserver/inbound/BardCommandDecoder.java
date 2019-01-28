package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommandDatum;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdEnum;
import com.alibaba.fastjson.JSON;
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
        CmdEnum c = CmdEnum.fromCode(cmd);
        datum.setCmd(c);
        datum.setPayload(JSON.parseObject(new String(content), c.getClz()));
        out.add(datum);
    }


}

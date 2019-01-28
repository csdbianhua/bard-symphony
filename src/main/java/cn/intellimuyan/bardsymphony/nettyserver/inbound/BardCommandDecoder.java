package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommandDatum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hason
 * @version 19-1-28
 */
@Service
@Order(0)
public class BardCommandDecoder extends ReplayingDecoder<Void> {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readInt();
        int cmd = in.readInt();
        byte[] content = new byte[length];
        in.readBytes(content);
        BardCommandDatum datum = new BardCommandDatum();
        datum.setLength(length);
        datum.setCmd(cmd);
        datum.setPayload(new String(content));
        out.add(datum);
    }


}

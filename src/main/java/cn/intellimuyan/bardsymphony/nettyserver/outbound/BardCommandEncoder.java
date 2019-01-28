package cn.intellimuyan.bardsymphony.nettyserver.outbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * @author hason
 * @version 19-1-28
 */
@Service
@Order
public class BardCommandEncoder extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

    }
}

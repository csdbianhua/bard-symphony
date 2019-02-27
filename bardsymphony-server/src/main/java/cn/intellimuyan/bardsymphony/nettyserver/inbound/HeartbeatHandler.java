package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            BardCommand cmd = new BardCommand();
            cmd.setCmd(CmdType.MSG);
            ctx.channel().writeAndFlush(cmd).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

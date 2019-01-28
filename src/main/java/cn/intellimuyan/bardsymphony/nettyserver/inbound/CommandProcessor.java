package cn.intellimuyan.bardsymphony.nettyserver.inbound;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommandDatum;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdEnum;
import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.PlayMsg;
import cn.intellimuyan.bardsymphony.service.ClientManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Service
@Order(1)
@ChannelHandler.Sharable
public class CommandProcessor extends ChannelInboundHandlerAdapter {

    private final ClientManager clientManager;

    public CommandProcessor(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        BardCommandDatum datum = (BardCommandDatum) msg;
        CmdEnum cmd = datum.getCmd();
        switch (cmd) {
            case JOIN:
                JoinMsg joinMsg = (JoinMsg) datum.getPayload();
                clientManager.addClient(new Player(ctx.channel(), joinMsg));
                break;
            case PLAY:
                PlayMsg playMsg = (PlayMsg) datum.getPayload();
                clientManager.sendPlayCommand(playMsg);
                break;

        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        clientManager.removeClient(ctx.channel());
        ctx.fireChannelInactive();
    }
}

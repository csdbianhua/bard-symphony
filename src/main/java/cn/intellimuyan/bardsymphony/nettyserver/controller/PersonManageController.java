package cn.intellimuyan.bardsymphony.nettyserver.controller;

import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.CmdMapping;
import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.NettyController;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.service.PlayerManager;

/**
 * @author hason
 * @version 19-1-29
 */
@NettyController
public class PersonManageController {

    private final PlayerManager clientManager;

    public PersonManageController(PlayerManager clientManager) {
        this.clientManager = clientManager;
    }

    @CmdMapping(mapping = CmdType.JOIN)
    public void join(Player player, JoinMsg joinMsg) {
        player.setName(joinMsg.getName());
        player.setInstrument(joinMsg.getInstrument());
    }

    @CmdMapping(mapping = CmdType.LEAVE)
    public void leave(Player player) {
        clientManager.removeClient(player.getChannel());
    }

}

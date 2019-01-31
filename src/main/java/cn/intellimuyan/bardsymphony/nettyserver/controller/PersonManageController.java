package cn.intellimuyan.bardsymphony.nettyserver.controller;

import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.CmdMapping;
import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.NettyController;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.QueryPlayerMsg;
import cn.intellimuyan.bardsymphony.service.PlayerManager;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hason
 * @version 19-1-29
 */
@NettyController
@Slf4j
public class PersonManageController {

    private final PlayerManager playerManager;

    public PersonManageController(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @CmdMapping(mapping = CmdType.JOIN)
    public void join(Player player, JoinMsg joinMsg) {
        player.setProfile(joinMsg);
        Player oldPlayer = playerManager.addPlayer(player);
        if (oldPlayer == null) {
            log.info("[乐手管理] 加入 ->  {}", player);
        } else {
            log.info("[乐手管理] 更新 {} -> {}", oldPlayer, player);
        }
    }

    @CmdMapping(mapping = CmdType.LEAVE)
    public void leave(Player player) {
        playerManager.removeClient(player.getChannel());
    }

    @CmdMapping(mapping = CmdType.QUERY_PLAYER,
            returning = CmdType.QUERY_PLAYER_RESULT)
    public QueryPlayerMsg players() {
        QueryPlayerMsg msg = new QueryPlayerMsg();
        msg.setPlayers(playerManager.players());
        return msg;
    }
}

package cn.intellimuyan.bardclient.director;

import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.model.Player;
import cn.intellimuyan.bardclient.model.msg.QueryPlayerMsg;
import cn.intellimuyan.bardclient.nettyclient.anno.CmdMapping;
import cn.intellimuyan.bardclient.nettyclient.anno.NettyController;

import java.util.List;

/**
 * @author hason
 * @version 19-1-29
 */
@NettyController
public class DirectorController {

    @CmdMapping(mapping = CmdType.QUERY_PLAYER_RESULT)
    public void playerQueryResult(QueryPlayerMsg msg) {
        List<Player> players = msg.getPlayers();
        DirectorWindow.notifyPlayersRefresh(players);
    }

}

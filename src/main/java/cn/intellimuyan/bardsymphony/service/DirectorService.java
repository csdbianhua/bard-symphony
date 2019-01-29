package cn.intellimuyan.bardsymphony.service;

import cn.intellimuyan.bardsymphony.nettyserver.model.BardCommand;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.DirectMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.PlayMsg;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

/**
 * 乐队指挥
 *
 * @author hason
 * @version 19-1-29
 */
@Service
public class DirectorService {

    private final PlayerManager playerManager;

    public DirectorService(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void sendPlayCommand(DirectMsg directorMsg) {
        Player player = playerManager.getPlayerByInstrument(directorMsg.getInstrument());
        PlayMsg msg = new PlayMsg();
        msg.setNote(directorMsg.getNote());
        BardCommand cmd = new BardCommand();
        cmd.setCmd(CmdType.PLAY);
        cmd.setPayload(JSON.toJSONString(msg));
        player.getChannel().writeAndFlush(cmd);
    }

}

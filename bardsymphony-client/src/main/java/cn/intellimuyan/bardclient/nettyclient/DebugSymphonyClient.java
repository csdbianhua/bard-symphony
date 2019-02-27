package cn.intellimuyan.bardclient.nettyclient;

import cn.intellimuyan.bardclient.director.DirectorWindow;
import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * @author hason
 * @version 19-1-31
 */
@Profile("debug")
@Service
@Slf4j
public class DebugSymphonyClient implements ISymphonyClient {
    @Override
    public void connect() {
        log.info("[DebugSymphonyClient]start connect");
    }

    @Override
    public void close() {
        log.info("[DebugSymphonyClient]close");
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public void sendCmd(CmdType cmdType, Object payload) {
        log.info("[DebugSymphonyClient]sendCmd {} - {}", cmdType, payload);
        if (cmdType == CmdType.QUERY_PLAYER) {
            Player player = new Player();
            player.setId("fake");
            player.setName("假人");
            DirectorWindow.notifyPlayersRefresh(Collections.singletonList(player));
        }
    }
}

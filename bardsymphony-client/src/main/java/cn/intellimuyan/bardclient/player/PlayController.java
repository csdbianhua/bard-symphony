package cn.intellimuyan.bardclient.player;

import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.model.msg.PlayMsg;
import cn.intellimuyan.bardclient.nettyclient.anno.CmdMapping;
import cn.intellimuyan.bardclient.nettyclient.anno.NettyController;

/**
 * @author hason
 * @version 19-1-29
 */
@NettyController
public class PlayController {

    private final LocalKeyboard localKeyboard;

    public PlayController(LocalKeyboard localKeyboard) {
        this.localKeyboard = localKeyboard;
    }

    @CmdMapping(mapping = CmdType.PLAY)
    public void play(PlayMsg playMsg) {
        localKeyboard.pressNote(playMsg.getNote());
    }

}

package cn.intellimuyan.bardclient.director;

import cn.intellimuyan.bardclient.base.IKeyboard;
import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.model.Player;
import cn.intellimuyan.bardclient.model.msg.DirectMsg;
import cn.intellimuyan.bardclient.nettyclient.ISymphonyClient;


public class RemoteKeyboard implements IKeyboard {

    private final ISymphonyClient client;
    private final Player player;

    public RemoteKeyboard(ISymphonyClient client,
                          Player player) {
        this.client = client;
        this.player = player;
    }


    @Override
    public void pressNote(int note) {
        DirectMsg msg = new DirectMsg();
        msg.setNote(note);
        msg.setId(player.getId());
        client.sendCmd(CmdType.DIRECT, msg);
    }
}

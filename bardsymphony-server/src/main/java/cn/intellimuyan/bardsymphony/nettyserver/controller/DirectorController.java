package cn.intellimuyan.bardsymphony.nettyserver.controller;

import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.CmdMapping;
import cn.intellimuyan.bardsymphony.nettyserver.framework.anno.NettyController;
import cn.intellimuyan.bardsymphony.nettyserver.model.CmdType;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.DirectMsg;
import cn.intellimuyan.bardsymphony.service.DirectorService;

/**
 * @author hason
 * @version 19-1-29
 */
@NettyController
public class DirectorController {

    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    /**
     * 指挥者发送弹奏命令
     */
    @CmdMapping(mapping = CmdType.DIRECT)
    public void direct(DirectMsg directMsg) {
        directorService.sendPlayCommand(directMsg);
    }

}

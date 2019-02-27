package cn.intellimuyan.bardclient.director.model;

import cn.intellimuyan.bardclient.base.PlayCommand;
import cn.intellimuyan.bardclient.model.Player;
import lombok.Data;

import java.util.List;

/**
 * @author hason
 * @version 19-2-1
 */
@Data
public class ChannelInfo {
    /**
     * 乐手
     */
    private Player player;
    /**
     * 命令
     */
    private List<PlayCommand> commands;
    /**
     * 通道名称
     */
    private String channel;
}

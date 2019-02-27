package cn.intellimuyan.bardclient.model;

import lombok.Data;

/**
 * 一条命令的数据结构
 *
 * @author hason
 * @version 19-1-28
 */
@Data
public class BardCommand {
    private CmdType cmd;
    private String payload;
}

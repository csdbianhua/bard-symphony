package cn.intellimuyan.bardsymphony.nettyserver.model;

import lombok.Data;

/**
 * 一条命令的数据结构
 *
 * @author hason
 * @version 19-1-28
 */
@Data
public class BardCommandDatum {
    private CmdEnum cmd;
    private Object payload;
}

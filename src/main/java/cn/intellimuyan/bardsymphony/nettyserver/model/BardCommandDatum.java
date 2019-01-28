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
    private int length;
    private int cmd;
    /**
     * 先使用json格式进行数据交互
     */
    private String payload;

}

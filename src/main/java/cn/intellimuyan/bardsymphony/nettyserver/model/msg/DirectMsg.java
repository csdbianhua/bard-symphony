package cn.intellimuyan.bardsymphony.nettyserver.model.msg;

import lombok.Data;

/**
 * @author hason
 * @version 19-1-29
 */
@Data
public class DirectMsg {

    private String note;
    /**
     * 乐手id
     */
    private String id;
}

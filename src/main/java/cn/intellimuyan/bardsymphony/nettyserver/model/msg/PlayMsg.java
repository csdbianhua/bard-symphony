package cn.intellimuyan.bardsymphony.nettyserver.model.msg;

import lombok.Data;

@Data
public class PlayMsg {
    /**
     * 音符
     */
    private String note;
    /**
     * 乐器
     */
    private String instrument;
}

package cn.intellimuyan.bardsymphony.nettyserver.model.msg;

import lombok.Data;

@Data
public class JoinMsg {
    /**
     * 我的名称
     */
    private String name;
    /**
     * 所使用的乐器
     */
    private String instrument;
}

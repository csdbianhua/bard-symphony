package cn.intellimuyan.bardsymphony.nettyserver.model;

import lombok.Getter;

/**
 * @author hason
 * @version 19-1-28
 */
@Getter
public enum CmdEnum {
    JOIN(0);

    private final int code;

    CmdEnum(int code) {
        this.code = code;
    }
}

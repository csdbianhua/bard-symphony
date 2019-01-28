package cn.intellimuyan.bardsymphony.nettyserver.model;

import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.PlayMsg;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.ReplayMsg;
import lombok.Getter;

/**
 * @author hason
 * @version 19-1-28
 */
@Getter
public enum CmdEnum {
    JOIN(0, JoinMsg.class),
    PLAY(1, PlayMsg.class),
    REPLAY(-1, ReplayMsg.class);

    private final int code;
    private final Class<?> clz;

    CmdEnum(int code, Class clz) {
        this.code = code;
        this.clz = clz;
    }

    public static CmdEnum fromClz(Class<?> clz) {
        for (CmdEnum value : CmdEnum.values()) {
            if (clz.isAssignableFrom(value.clz)) {
                return value;
            }
        }
        throw new UnsupportedOperationException();
    }

    public static CmdEnum fromCode(int cmd) {
        for (CmdEnum c : CmdEnum.values()) {
            if (c.code == cmd) {
                return c;
            }
        }
        throw new UnsupportedOperationException();
    }}

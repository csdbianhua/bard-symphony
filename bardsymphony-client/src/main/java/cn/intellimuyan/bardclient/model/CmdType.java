package cn.intellimuyan.bardclient.model;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hason
 * @version 19-1-28
 */
@Getter
public enum CmdType {
    NONE(0),
    JOIN(1),
    LEAVE(2),
    PLAY(3),
    DIRECT(4),
    QUERY_PLAYER(5),
    QUERY_PLAYER_RESULT(-5),
    MSG(6);

    private static final Map<Integer, CmdType> MAP;

    static {
        TreeMap<Integer, CmdType> map = new TreeMap<>();
        for (CmdType value : CmdType.values()) {
            if (map.put(value.code, value) != null) {
                throw new IllegalStateException("CmdType code 重复 : " + value.getCode());
            }
        }
        MAP = Collections.unmodifiableMap(map);
    }

    private final int code;

    CmdType(int code) {
        this.code = code;
    }

    public static CmdType fromCode(int cmd) {
        return MAP.get(cmd);
    }
}

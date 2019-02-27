package cn.intellimuyan.bardclient.base;

import cn.intellimuyan.bardclient.util.MusicUtils;
import lombok.Data;

@Data
public class PlayCommand {
    public enum Type {
        WAIT, RELEASE, PLUCK;
    }

    public static final PlayCommand RELEASE = new PlayCommand(Type.RELEASE);

    private final Type type;
    private final int content;
    private String str;

    public PlayCommand(Type type, long content) {
        this(type, (int) content);
    }

    public PlayCommand(Type type) {
        this(type, 0);
    }

    public PlayCommand(Type type, int content) {
        this.type = type;
        this.content = content;
        switch (type) {
            case WAIT:
                str = "wait : " + content + "ms";
                break;
            case PLUCK:
                str = "pluck : " + MusicUtils.numberToNote(content);
                break;
            case RELEASE:
                str = "release";
                break;
        }
    }

    @Override
    public String toString() {
        return str;
    }
}

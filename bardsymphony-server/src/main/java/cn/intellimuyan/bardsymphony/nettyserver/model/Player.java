package cn.intellimuyan.bardsymphony.nettyserver.model;

import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import com.alibaba.fastjson.annotation.JSONField;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(of = "channel")
public class Player {
    @JSONField(serialize = false, deserialize = false)
    private final Channel channel;
    private String name = "unknown";
    private String instrument = "unknown";

    public Player(Channel channel) {
        this.channel = channel;
    }

    public void setProfile(JoinMsg joinMsg) {
        this.name = joinMsg.getName();
        this.instrument = joinMsg.getInstrument();
    }

    public String getId() {
        return channel.id().asShortText();
    }

    @Override
    public String toString() {
        return String.format("{%s} - %s[%s]", instrument, name, getId());
    }
}

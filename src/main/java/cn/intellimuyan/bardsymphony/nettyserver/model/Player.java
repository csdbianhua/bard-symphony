package cn.intellimuyan.bardsymphony.nettyserver.model;

import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(of = "channel")
public class Player {
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

    @Override
    public String toString() {
        return String.format("{%s} - %s", instrument, name);
    }
}

package cn.intellimuyan.bardsymphony.nettyserver.model;

import cn.intellimuyan.bardsymphony.nettyserver.model.msg.JoinMsg;
import io.netty.channel.Channel;
import lombok.Data;


@Data
public class Player {
    private final Channel channel;
    private String name;
    private String instrument;

    public Player(Channel channel, JoinMsg joinMsg) {
        this.channel = channel;
        this.name = joinMsg.getName();
        this.instrument = joinMsg.getInstrument();
    }

    @Override
    public String toString() {
        return String.format("{%s} - %s", instrument, name);
    }
}

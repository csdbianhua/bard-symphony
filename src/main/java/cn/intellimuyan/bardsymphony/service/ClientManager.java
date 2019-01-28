package cn.intellimuyan.bardsymphony.service;

import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.nettyserver.model.msg.PlayMsg;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class ClientManager {

    private final Map<String, Set<Player>> instrumentPlayerMap = new HashMap<>();
    private final Map<Channel, Player> channelPlayerMap = new HashMap<>();

    public synchronized void addClient(Player player) {
        Player oldPlayer = channelPlayerMap.get(player.getChannel());
        if (oldPlayer != null) {
            oldPlayer.setName(player.getName());
            oldPlayer.setInstrument(player.getInstrument());
            player = oldPlayer;
        }
        channelPlayerMap.put(player.getChannel(), player);
        Set<Player> set = instrumentPlayerMap.computeIfAbsent(player.getInstrument(), (a) -> new HashSet<>());
        set.add(player);
        log.info("[乐手加入] {}", player);
    }

    public void sendPlayCommand(PlayMsg playMsg) {
        String instrument = playMsg.getInstrument();
        Set<Player> players = instrumentPlayerMap.get(instrument);
        if (players == null || players.isEmpty()) {
            players = instrumentPlayerMap.values().iterator().next();
        }
        Iterator<Player> it = players.iterator();
        int idx = ThreadLocalRandom.current().nextInt(players.size());
        for (int i = 0; i < idx; i++) {
            it.next();
        }
        sendPlayCommand(it.next(), playMsg);
    }

    private void sendPlayCommand(Player player, PlayMsg msg) {
        Channel c = player.getChannel();
        c.writeAndFlush(msg);
    }

    public synchronized void removeClient(Channel channel) {
        Player player = channelPlayerMap.remove(channel);
        if (player == null) {
            return;
        }
        Set<Player> players = instrumentPlayerMap.get(player.getInstrument());
        if (players != null) {
            players.remove(player);
            if (players.isEmpty()) {
                instrumentPlayerMap.remove(player.getInstrument());
            }
        }
        log.info("[乐手离开] {}", player);

    }
}

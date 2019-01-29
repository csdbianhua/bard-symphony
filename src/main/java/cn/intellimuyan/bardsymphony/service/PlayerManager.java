package cn.intellimuyan.bardsymphony.service;

import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import io.netty.channel.Channel;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class PlayerManager {

    private final Map<String, Set<Player>> instrumentPlayerMap = new HashMap<>();
    private final Map<Channel, Player> channelPlayerMap = new HashMap<>();

    @Synchronized("channelPlayerMap")
    public Player addPlayer(Player player) {
        Player oldPlayer = channelPlayerMap.get(player.getChannel());
        channelPlayerMap.put(player.getChannel(), player);
        Set<Player> set = instrumentPlayerMap.computeIfAbsent(player.getInstrument(), (a) -> new HashSet<>());
        set.add(player);
        return oldPlayer;
    }

    public Collection<Player> players() {
        return Collections.unmodifiableCollection(channelPlayerMap.values());
    }

    public Player getPlayer(Channel channel) {
        Player result = channelPlayerMap.get(channel);
        if (result == null) {
            return new Player(channel);
        } else {
            return result;
        }
    }

    /**
     * 根据乐器种类获取一名乐手，如果此乐器没有乐手，则随机选择其他乐手
     *
     * @param instrument 乐器种类
     * @return 乐手
     */
    public Player getPlayerByInstrument(String instrument) {
        Set<Player> players = instrumentPlayerMap.get(instrument);
        if (players == null || players.isEmpty()) {
            players = instrumentPlayerMap.values().iterator().next();
        }
        Iterator<Player> it = players.iterator();
        int idx = ThreadLocalRandom.current().nextInt(players.size());
        for (int i = 0; i < idx; i++) {
            it.next();
        }
        return it.next();
    }

    @Synchronized("channelPlayerMap")
    public void removeClient(Channel channel) {
        Player player = channelPlayerMap.remove(channel);
        if (player == null) {
            return;
        }
        Set<Player> players = instrumentPlayerMap.get(player.getInstrument());
        if (players != null) {
            players.removeIf(p -> p.getChannel() == channel);
            if (players.isEmpty()) {
                instrumentPlayerMap.remove(player.getInstrument());
            }
        }
        log.info("[乐手离开] {}", player);

    }
}

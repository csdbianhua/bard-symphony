package cn.intellimuyan.bardsymphony.service;

import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import io.netty.channel.Channel;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class PlayerManager {

    private final Map<String, Set<Player>> instrumentPlayerMap = new HashMap<>();
    private final Map<String, Player> channelPlayerMap = new HashMap<>();

    @Synchronized("channelPlayerMap")
    public Optional<Player> addPlayer(Player player) {
        String id = player.getChannel().id().asShortText();
        Optional<Player> oldPlayerOpt = getPlayer(id);
        channelPlayerMap.put(id, player);
        Set<Player> set = instrumentPlayerMap.computeIfAbsent(player.getInstrument(), (a) -> new HashSet<>());
        set.add(player);
        return oldPlayerOpt;
    }

    public Collection<Player> players() {
        return new ArrayList<>(channelPlayerMap.values());
    }

    public Player getOrNewPlayer(Channel channel) {
        Optional<Player> opt = getPlayer(channel.id().asShortText());
        return opt.orElseGet(() -> new Player(channel));
    }

    /**
     * 根据id获取一名乐手
     *
     * @param id channel的id
     * @return 乐手
     */
    public Optional<Player> getPlayer(String id) {
        return Optional.ofNullable(channelPlayerMap.get(id));
    }

    @Synchronized("channelPlayerMap")
    public void removeClient(Channel channel) {
        Player player = channelPlayerMap.remove(channel.id().asShortText());
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

    }
}

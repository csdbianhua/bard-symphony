package cn.intellimuyan.bardsymphony.httpserver.controller;

import cn.intellimuyan.bardsymphony.nettyserver.model.Player;
import cn.intellimuyan.bardsymphony.service.PlayerManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author hason
 * @version 19-1-29
 */
@RestController
@RequestMapping("api")
public class AdminApi {

    private final PlayerManager playerManager;

    public AdminApi(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @GetMapping("players")
    public Collection<Player> players() {
        return playerManager.players();
    }
}

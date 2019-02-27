package cn.intellimuyan.bardclient.player;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static javafx.application.Application.launch;

@Component
@Order
@Slf4j
public class PlayerBooster {

    @Autowired
    private ApplicationContext ctx;

    @PostConstruct
    public void start() {
        Thread guiThread = new Thread(() -> {
            log.info("[GUI]startup");
            try {
                PlayerWindow.ctx = ctx;
                launch(PlayerWindow.class);
                log.info("[GUI]shutdown");
                System.exit(0);
            } catch (RuntimeException e) {
                log.error("[GUI]shutdown", e);
                System.exit(1);
            }
        });
        guiThread.setName("guiThread");
        guiThread.start();
    }
}

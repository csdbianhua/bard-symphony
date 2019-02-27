package cn.intellimuyan.bardclient.director;


import cn.intellimuyan.bardclient.model.CmdType;
import cn.intellimuyan.bardclient.nettyclient.ISymphonyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

import static javafx.application.Application.launch;

@Component
@Order
@Slf4j
public class DirectorBooster {

    @Autowired
    private ISymphonyClient client;

    @Autowired
    private ApplicationContext ctx;

    private volatile static boolean shutdown;

    @PostConstruct
    public void start() {
        client.connect();

        Thread guiThread = new Thread(() -> {
            log.info("[GUI]startup");
            try {
                DirectorWindow.ctx = ctx;
                launch(DirectorWindow.class);
                log.info("[GUI]shutdown");
                System.exit(0);
            } catch (RuntimeException e) {
                log.error("[GUI]shutdown", e);
                System.exit(1);
            } finally {
                shutdown = true;
            }
        });
        guiThread.setName("guiThread");
        guiThread.start();


        Thread heartBeatThread = new Thread(() -> {
            while (!shutdown) {
                try {
                    if (client.isOnline()) {
                        client.sendCmd(CmdType.QUERY_PLAYER, null);
                    }
                    TimeUnit.SECONDS.sleep(5);
                } catch (Exception e) {
                    log.warn("[HeartBeat]查询乐手失败", e);
                }
            }
        });
        heartBeatThread.setName("heartBeatThread");
        heartBeatThread.setDaemon(true);
        heartBeatThread.start();
    }
}

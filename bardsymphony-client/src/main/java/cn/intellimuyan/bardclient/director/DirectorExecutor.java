package cn.intellimuyan.bardclient.director;

import cn.intellimuyan.bardclient.base.Performer;
import cn.intellimuyan.bardclient.base.PlayCommand;
import cn.intellimuyan.bardclient.director.model.ChannelInfo;
import cn.intellimuyan.bardclient.director.model.PlayConfig;
import cn.intellimuyan.bardclient.nettyclient.ISymphonyClient;
import javafx.application.Platform;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 导演执行器
 *
 * @author hason
 * @version 19-2-1
 */
@Service
public class DirectorExecutor {
    public enum Stat {
        IDLE,
        RUNNING,
        SUSPEND; // 暂未实现

    }

    public volatile Stat stat = Stat.IDLE;

    private volatile long startTime = 0L;

    private final ISymphonyClient client;

    private Thread countDownThread;

    private CyclicBarrier barrier;

    private List<Worker> threadPool = Collections.synchronizedList(new LinkedList<>());

    private List<Consumer<Stat>> listeners = new LinkedList<>();

    private Timer timer;

    public DirectorExecutor(ISymphonyClient client) {
        this.client = client;
    }


    public void addListeners(Consumer<Stat> listener) {
        listeners.add(listener);
    }

    private void setStat(Stat stat) {
        this.stat = stat;
        for (Consumer<Stat> listener : listeners) {
            listener.accept(this.stat);
        }
    }

    public synchronized void play(List<ChannelInfo> list, PlayConfig config) {
        if (this.stat == Stat.RUNNING) {
            return;
        }
        int delay = config.getStartDelay();
        setStat(Stat.RUNNING);
        barrier = new CyclicBarrier(list.size() + 1);
        this.countDownThread = new Thread(() -> {
            try {
                if (delay > 0) {
                    TimeUnit.SECONDS.sleep(delay);
                }
                if (barrier != null) {
                    barrier.await();
                }
            } catch (BrokenBarrierException | InterruptedException ignored) {
            }
            startTime = System.currentTimeMillis();
            if (stat != Stat.RUNNING) {
                return;
            }
            this.timer = new Timer(true);
            double waitMultiplier = config.getWaitMultiplier();
            double totalMills = config.getMidiParser().getMills() * waitMultiplier;
            DirectorWindow.setTotalTime(totalMills);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    double pastTime = System.currentTimeMillis() - startTime;
                    if (pastTime >= totalMills) {
                        Platform.runLater(() -> DirectorWindow.setPastTime(totalMills, 1));
                        timer.cancel();
                        setStat(Stat.IDLE);
                    } else {
                        Platform.runLater(() -> DirectorWindow.setPastTime(pastTime, pastTime / totalMills));
                    }

                }
            }, 0, 50L);
        });
        countDownThread.start();
        for (ChannelInfo channelInfo : list) {
            Worker worker = new Worker(channelInfo, barrier, config);
            worker.start();
            threadPool.add(worker);
        }
    }

    @PreDestroy
    public synchronized void stop() {
        if (stat == Stat.IDLE) {
            return;
        }
        setStat(Stat.IDLE);
        barrier.reset();
        barrier = null;
        if (countDownThread != null) {
            countDownThread.interrupt();
        }
        if (timer != null) {
            timer.cancel();
        }
        Iterator<Worker> it = threadPool.iterator();
        while (it.hasNext()) {
            Worker worker = it.next();
            worker.close();
            it.remove();
        }
        Platform.runLater(() -> DirectorWindow.setPastTime(0, 0));
    }

    private class Worker extends Thread {

        private final ChannelInfo info;
        private final CyclicBarrier barrier;
        private final PlayConfig config;
        private boolean running = true;


        Worker(ChannelInfo info, CyclicBarrier barrier, PlayConfig config) {
            this.info = info;
            this.barrier = barrier;
            this.config = config;
        }

        public void close() {
            this.running = false;
            this.interrupt();
        }

        @Override
        public void run() {
            List<PlayCommand> commands = info.getCommands();
            Performer performer = new Performer(new RemoteKeyboard(client, info.getPlayer()), config.getWaitMultiplier());
            try {
                barrier.await();
                for (PlayCommand command : commands) {
                    if (!running) {
                        break;
                    }
                    performer.play(command);
                }
            } catch (InterruptedException | BrokenBarrierException e) {
                // 被终止
            }
        }
    }

}

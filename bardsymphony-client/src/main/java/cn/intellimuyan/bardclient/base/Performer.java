package cn.intellimuyan.bardclient.base;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Performer {

    private final IKeyboard keyboard;
    private final double waitMultiplier;

    public Performer(IKeyboard keyboard, double waitMultiplier) {
        this.keyboard = keyboard;
        this.waitMultiplier = waitMultiplier;
    }


    @SneakyThrows
    public void play(PlayCommand command) {


        System.out.println("\n--- " + command + " ---");

        PlayCommand.Type type = command.getType();
        switch (type) {
            case RELEASE:
                return;
            case PLUCK:
                keyboard.pressNote(command.getContent());
                return;
            case WAIT:
                long waitTime = command.getContent();
                waitTime = (long) (waitTime * waitMultiplier);
                System.out.println("Waiting for " + waitTime);
                Thread.sleep(waitTime);
        }


    }

}

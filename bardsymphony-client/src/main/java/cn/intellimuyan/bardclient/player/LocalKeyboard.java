package cn.intellimuyan.bardclient.player;

import cn.intellimuyan.bardclient.base.IKeyboard;
import cn.intellimuyan.bardclient.util.MusicUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LocalKeyboard implements IKeyboard {

    private Robot r;

    public LocalKeyboard() throws AWTException {
        r = new Robot();
    }

    private static String[] NOTES = {"c(-1)", "c", "c(+1)",
            "c#(-1)", "c#", "c#(+1)",
            "d(-1)", "d", "d(+1)",
            "eb(-1)", "eb", "eb(+1)",
            "e(-1)", "e", "e(+1)",
            "f(-1)", "f", "f(+1)",
            "f#(-1)", "f#", "f#(+1)",
            "g(-1)", "g", "g(+1)",
            "g#(-1)", "g#", "g#(+1)",
            "a(-1)", "a", "a(+1)",
            "bb(-1)", "bb", "bb(+1)",
            "b(-1)", "b", "b(+1)",
            "c(+2)"
    };

    private static Map<Integer, Integer> FULL_MAP = new HashMap<>();

    private static int[] DEFAULT_KEY = {KeyEvent.VK_Y, KeyEvent.VK_9, KeyEvent.VK_1, //C
            KeyEvent.VK_V, KeyEvent.VK_K, KeyEvent.VK_D, //C#
            KeyEvent.VK_U, KeyEvent.VK_0, KeyEvent.VK_2, //D
            KeyEvent.VK_B, KeyEvent.VK_L, KeyEvent.VK_F, //Eb
            KeyEvent.VK_I, KeyEvent.VK_Q, KeyEvent.VK_3, //E
            KeyEvent.VK_O, KeyEvent.VK_W, KeyEvent.VK_4, //F
            KeyEvent.VK_N, KeyEvent.VK_Z, KeyEvent.VK_G, //F#
            KeyEvent.VK_P, KeyEvent.VK_E, KeyEvent.VK_5, //G
            KeyEvent.VK_M, KeyEvent.VK_X, KeyEvent.VK_H, //G#
            KeyEvent.VK_A, KeyEvent.VK_R, KeyEvent.VK_6, //A
            KeyEvent.VK_COMMA, KeyEvent.VK_C, KeyEvent.VK_J, //Bb
            KeyEvent.VK_S, KeyEvent.VK_T, KeyEvent.VK_7, //B
            KeyEvent.VK_8}; //C+2


    public void pressNote(int note) {
        Integer keyToPress = FULL_MAP.getOrDefault(note, -1);
        if (keyToPress == -1) {
            log.warn("Key does not exist : {}", MusicUtils.numberToNote(note));
            return;
        }
        r.keyPress(keyToPress);
        r.delay(1);
        r.keyRelease(keyToPress);
    }


    @PostConstruct
    public void init() {
        List<String> lines;
        try {
            Path path = Paths.get("key_map.config");
            if (Files.exists(path)) {
                lines = Files.readAllLines(path);
                for (String line : lines) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    String[] part = line.split("=", 2);
                    FULL_MAP.put(MusicUtils.noteToNumber(part[0]), KeyEvent.getExtendedKeyCodeForChar(part[1].charAt(0)));
                }
                for (Map.Entry<Integer, Integer> entry : FULL_MAP.entrySet()) {
                    log.info("[LocalKeyboard]{} -> {}", MusicUtils.numberToNote(entry.getKey()), KeyEvent.getKeyText(entry.getValue()));
                }
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("[LocalKeyboard]use default");
        for (int i = 0; i < DEFAULT_KEY.length; i++) {
            FULL_MAP.put(MusicUtils.noteToNumber(NOTES[i]), DEFAULT_KEY[i]);
        }
        for (Map.Entry<Integer, Integer> entry : FULL_MAP.entrySet()) {
            log.info("[LocalKeyboard]{} -> {}", MusicUtils.numberToNote(entry.getKey()), KeyEvent.getKeyText(entry.getValue()));
        }
    }
}

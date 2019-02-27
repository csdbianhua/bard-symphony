package cn.intellimuyan.bardclient.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MusicUtils {
    private static final Pattern NOTE_PATTERN = Pattern.compile("(\\w[\\w#]?)(\\(.*\\))?");

    public static int noteToNumber(String note) {
        Matcher m = NOTE_PATTERN.matcher(note);
        Assert.isTrue(m.find(), "valid note : " + note);
        String baseNote = m.group(1);
        int key;
        baseNote = baseNote.toLowerCase();
        switch (baseNote) {
            case "c":
                key = 0;
                break;
            case "db":
            case "c#":
                key = 1;
                break;
            case "d":
                key = 2;
                break;
            case "eb":
            case "d#":
                key = 3;
                break;
            case "e":
                key = 4;
                break;
            case "f":
                key = 5;
                break;
            case "gb":
            case "f#":
                key = 6;
                break;
            case "g":
                key = 7;
                break;
            case "g#":
            case "ab":
                key = 8;
                break;
            case "a":
                key = 9;
                break;
            case "a#":
            case "bb":
                key = 10;
                break;
            case "b":
                key = 11;
                break;
            default:
                throw new IllegalArgumentException(note);
        }
        int octave = 4;
        String add = m.group(2);
        if (add != null) {
            String n = add.substring(1, add.length() - 1);
            int num = Integer.parseInt(n);
            octave += num;
        }
        key += octave * 12;
        return key;
    }

    public static String numberToNote(int num) {
        int octave = num / 12;
        int key = num % 12;
        String baseNote;
        switch (key) {
            case 0:
                baseNote = "C";
                break;
            case 1:
                baseNote = "C#";
                break;
            case 2:
                baseNote = "D";
                break;
            case 3:
                baseNote = "Eb";
                break;
            case 4:
                baseNote = "E";
                break;
            case 5:
                baseNote = "F";
                break;
            case 6:
                baseNote = "F#";
                break;
            case 7:
                baseNote = "G";
                break;
            case 8:
                baseNote = "G#";
                break;
            case 9:
                baseNote = "A";
                break;
            case 10:
                baseNote = "Bb";
                break;
            case 11:
                baseNote = "B";
                break;
            default:
                throw new IllegalArgumentException(String.valueOf(num));

        }
        int add = octave - 4;
        return add == 0 ? baseNote : String.format("%s(%+d)", baseNote, add);
    }

}

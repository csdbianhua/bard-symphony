package cn.intellimuyan.bardclient.director.model;

import cn.intellimuyan.bardclient.base.MidiParser;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PlayConfig {
    private int startDelay;
    private double waitMultiplier;
    private MidiParser midiParser;
}

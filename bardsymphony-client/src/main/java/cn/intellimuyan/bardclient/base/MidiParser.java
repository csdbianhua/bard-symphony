package cn.intellimuyan.bardclient.base;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Data
@Slf4j
public class MidiParser {

    private static final int NOTE_ON = 0x90;
    private static final int NOTE_OFF = 0x80;

    private String[] instruments;
    private List<String> shownInstruments;
    private Map<String, List<PlayCommand>> instrumentCommandMap = new HashMap<>();
    private List<List<PlayCommand>> sheets;

    private double ticksSpan;
    private double mills;


    public MidiParser(File file) throws InvalidMidiDataException, IOException, MidiUnavailableException {
        this.sheets = new ArrayList<>(16);
        for (int i = 0; i < 16; i++) {
            sheets.add(new LinkedList<>());
        }
        Sequence sequence = MidiSystem.getSequence(file);
        fillInstruments(sequence);
        fillNotes(sequence);
    }

    private void fillInstruments(Sequence sequence) throws MidiUnavailableException {
        Synthesizer syn = MidiSystem.getSynthesizer();
        syn.open();
        Instrument[] instr = syn.getDefaultSoundbank().getInstruments();
        log.info("DefaultInstruments:{}", Arrays.toString(instr));
        instruments = new String[16];

        for (Track track : sequence.getTracks()) {

            for (int i = 0; i < track.size(); i++) {

                MidiEvent event = track.get(i);


                MidiMessage message = event.getMessage();

                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        log.info("data:{}", sm.getData1());
                        Pattern pattern = Pattern.compile("Instrument: (.*?)  +bank.*");
                        Matcher matcher = pattern.matcher(instr[sm.getData1()].toString());

                        String instrument;
                        if (matcher.matches()) {
                            instrument = matcher.group(1);
                        } else {
                            instrument = "Unknown";
                        }
                        if (instruments[sm.getChannel()] == null)
                            instruments[sm.getChannel()] = (sm.getChannel() + 1) + ". " + instrument;
                        else if (!instruments[sm.getChannel()].contains(instrument)) {
                            instruments[sm.getChannel()] += ", " + instrument;
                        }
                    }
                }
            }
        }
        shownInstruments = new ArrayList<>();
        for (String instrument : instruments) {
            if (instrument != null) {
                shownInstruments.add(instrument);
            }
        }
    }


    private long ticksToMillis(long ticks) {
        return (long) (ticks * ticksSpan);
    }

    private void fillNotes(Sequence sequence) {
        this.mills = sequence.getMicrosecondLength() / 1000d;
        this.ticksSpan = mills / sequence.getTickLength();
        log.info("Sequence mills: " + mills);
        log.info("Sequence ticks: " + sequence.getTickLength());
        log.info("ticksSpan(ms/tick): " + ticksSpan);

        for (Track track : sequence.getTracks()) {

            long prevTick = 0;
            ShortMessage sm;
            int lastKey = 0;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);

                MidiMessage message = event.getMessage();
                if (message instanceof ShortMessage) {
                    sm = (ShortMessage) message;
                    List<PlayCommand> commands = sheets.get(sm.getChannel());
                    long ticks = event.getTick() - prevTick;
                    if ((sm.getCommand() == NOTE_ON) && (sm.getData2() > 0)) {
                        if (ticks > 0) {
                            commands.add(new PlayCommand(PlayCommand.Type.WAIT, ticksToMillis(ticks)));
                        }
                        prevTick = event.getTick();
                        int key = sm.getData1();
                        lastKey = key;
                        commands.add(new PlayCommand(PlayCommand.Type.PLUCK, key));
                    } else if (sm.getCommand() == NOTE_OFF || sm.getData2() == 0) {
                        int key = sm.getData1();
                        if (lastKey == key) {
                            if (ticks != 0) {
                                commands.add(new PlayCommand(PlayCommand.Type.WAIT, ticksToMillis(ticks)));
                            }
                            commands.add(PlayCommand.RELEASE);
                            prevTick = event.getTick();
                        }
                        lastKey = 0;
                    }
                }

            }
        }


        for (int instrumentId = 0; instrumentId < instruments.length; instrumentId++) {
            instrumentCommandMap.put(instruments[instrumentId], sheets.get(instrumentId));
        }

    }

    public List<PlayCommand> getSheet(String instrument) {
        return instrumentCommandMap.getOrDefault(instrument, Collections.emptyList());
    }


}

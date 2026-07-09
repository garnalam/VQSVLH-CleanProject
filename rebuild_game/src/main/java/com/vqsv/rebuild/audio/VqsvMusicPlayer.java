package com.vqsv.rebuild.audio;

import com.vqsv.rebuild.resource.AssetPaths;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequencer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VqsvMusicPlayer {
    private static Sequencer sequencer;
    private static boolean requested;
    private static String currentName = "";

    private VqsvMusicPlayer() {
    }

    public static synchronized void startLoop(AssetPaths assets, String soundName) {
        requested = true;
        if (Boolean.getBoolean("vqsv.audio.disabled")) {
            currentName = soundName;
            return;
        }
        try {
            Path path = assets.soundOriginal(soundName + ".mid");
            if (!Files.isRegularFile(path)) {
                System.err.println("VQSV music missing: " + path);
                return;
            }
            if (sequencer != null && sequencer.isOpen()) {
                if (sequencer.isRunning() && soundName.equals(currentName)) {
                    return;
                }
                sequencer.stop();
                sequencer.close();
            }
            currentName = soundName;
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(MidiSystem.getSequence(path.toFile()));
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
        } catch (Exception ex) {
            System.err.println("VQSV music unavailable: " + ex.getMessage());
        }
    }

    public static synchronized void stop() {
        requested = false;
        currentName = "";
        if (sequencer == null) {
            return;
        }
        try {
            sequencer.stop();
            sequencer.close();
        } catch (Exception ignored) {
        } finally {
            sequencer = null;
        }
    }

    public static synchronized boolean requestedForSmoke() {
        return requested;
    }

    public static synchronized String currentNameForSmoke() {
        return currentName;
    }
}

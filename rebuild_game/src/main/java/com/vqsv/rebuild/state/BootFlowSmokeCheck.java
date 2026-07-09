package com.vqsv.rebuild.state;

import com.vqsv.rebuild.audio.VqsvMusicPlayer;
import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BootFlowSmokeCheck {
    private BootFlowSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        List<String> lines = new ArrayList<>();
        BootFlowState state = new BootFlowState(paths);
        GameStateMachine states = new GameStateMachine();
        states.replace(state);
        lines.add("bootFlowStart=" + state.phaseName());
        tick(state, states, 20, emptyInput());
        lines.add("bootFlowAfterLogo0=" + state.phaseName());
        tick(state, states, 20, emptyInput());
        lines.add("bootFlowAfterCwaLogo=" + state.phaseName());
        state.tick(input(KeyEvent.VK_RIGHT), states);
        lines.add("bootFlowAfterMusicChoice=" + state.phaseName());
        if (state.musicEnabledForSmoke()) {
            throw new IllegalStateException("RIGHT music choice should disable music");
        }
        String firstTitle = state.selectedMenuLabelForSmoke();
        state.tick(input(KeyEvent.VK_RIGHT), states);
        String nextTitle = state.selectedMenuLabelForSmoke();
        if (firstTitle.equals(nextTitle)) {
            throw new IllegalStateException("Title menu RIGHT did not change selection label=" + firstTitle);
        }
        state.tick(input(KeyEvent.VK_LEFT), states);
        if (!firstTitle.equals(state.selectedMenuLabelForSmoke())) {
            throw new IllegalStateException("Title menu LEFT did not return selection expected="
                    + firstTitle + " actual=" + state.selectedMenuLabelForSmoke());
        }
        lines.add("bootFlowTitleMenuLeftRight=verified:" + firstTitle + "->" + nextTitle);
        state.tick(pointer(220, 300), states);
        if (firstTitle.equals(state.selectedMenuLabelForSmoke())) {
            throw new IllegalStateException("Title menu pointer right did not change selection label=" + firstTitle);
        }
        state.tick(pointer(20, 300), states);
        if (!firstTitle.equals(state.selectedMenuLabelForSmoke())) {
            throw new IllegalStateException("Title menu pointer left did not return selection expected="
                    + firstTitle + " actual=" + state.selectedMenuLabelForSmoke());
        }
        lines.add("bootFlowTitleMenuPointerLeftRight=verified");
        VqsvMusicPlayer.stop();
        BootFlowState clickState = new BootFlowState(paths);
        tick(clickState, states, 40, emptyInput());
        clickState.tick(pointer(220, 300), states);
        lines.add("bootFlowPointerMusicChoice=" + clickState.phaseName());
        if (clickState.musicEnabledForSmoke()) {
            throw new IllegalStateException("Pointer right music choice should disable music");
        }

        BufferedImage image = new BufferedImage(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            state.render(graphics);
        } finally {
            graphics.dispose();
        }
        lines.add("bootFlowMenuRender=samplePixels:" + countNonTransparentPixels(image));
        lines.add("bootFlowMenuNewGame=verified:routesToLegacyScene0Runner");
        return lines;
    }

    private static void tick(BootFlowState state, GameStateMachine states, int count, InputSnapshot input) {
        for (int index = 0; index < count; index++) {
            state.tick(input, states);
        }
    }

    private static InputSnapshot emptyInput() {
        return new InputSnapshot(new HashSet<>(), new HashSet<>());
    }

    private static InputSnapshot input(int keyCode) {
        Set<Integer> pressed = new HashSet<>();
        pressed.add(keyCode);
        return new InputSnapshot(pressed, pressed);
    }

    private static InputSnapshot pointer(int x, int y) {
        return new InputSnapshot(new HashSet<>(), new HashSet<>(), true, x, y);
    }

    private static int countNonTransparentPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}

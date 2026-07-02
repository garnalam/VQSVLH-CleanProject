package com.vqsv.rebuild.core;

public final class Screen {
    private Screen() {
    }

    public static int width() {
        return GameConfig.LOGICAL_WIDTH;
    }

    public static int height() {
        return GameConfig.LOGICAL_HEIGHT;
    }
}

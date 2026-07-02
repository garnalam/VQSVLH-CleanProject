package com.vqsv.rebuild.input;

import java.awt.event.KeyEvent;
import java.util.Set;

public final class InputSnapshot {
    private final Set<Integer> down;
    private final Set<Integer> pressed;
    private final boolean pointerPressed;
    private final int pointerX;
    private final int pointerY;

    public InputSnapshot(Set<Integer> down, Set<Integer> pressed) {
        this(down, pressed, false, -1, -1);
    }

    public InputSnapshot(Set<Integer> down, Set<Integer> pressed, boolean pointerPressed, int pointerX, int pointerY) {
        this.down = Set.copyOf(down);
        this.pressed = Set.copyOf(pressed);
        this.pointerPressed = pointerPressed;
        this.pointerX = pointerX;
        this.pointerY = pointerY;
    }

    public boolean isDown(int keyCode) {
        return down.contains(keyCode);
    }

    public boolean wasPressed(int keyCode) {
        return pressed.contains(keyCode);
    }

    public boolean confirmPressed() {
        return wasPressed(KeyEvent.VK_0) || wasPressed(KeyEvent.VK_ENTER) || wasPressed(KeyEvent.VK_SPACE);
    }

    public boolean softLeftPressed() {
        return wasPressed(KeyEvent.VK_F1) || wasPressed(KeyEvent.VK_LEFT)
                || pointerPressed && pointerX >= 0 && pointerX <= 40 && pointerY >= 280 && pointerY <= 320;
    }

    public boolean softRightPressed() {
        return wasPressed(KeyEvent.VK_F2) || wasPressed(KeyEvent.VK_RIGHT)
                || pointerPressed && pointerX >= 200 && pointerX <= 240 && pointerY >= 280 && pointerY <= 320;
    }

    public boolean backPressed() {
        return wasPressed(KeyEvent.VK_ESCAPE) || wasPressed(KeyEvent.VK_BACK_SPACE);
    }

    public boolean pointerPressed() {
        return pointerPressed;
    }

    public int pointerX() {
        return pointerX;
    }

    public int pointerY() {
        return pointerY;
    }
}

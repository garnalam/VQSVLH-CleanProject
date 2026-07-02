package com.vqsv.rebuild.input;

import java.util.HashSet;
import java.util.Set;

public final class InputState {
    private final Set<Integer> down = new HashSet<>();
    private final Set<Integer> pressed = new HashSet<>();
    private boolean pointerPressed;
    private int pointerX = -1;
    private int pointerY = -1;

    public synchronized void setKey(int keyCode, boolean isDown) {
        if (isDown) {
            if (down.add(keyCode)) {
                pressed.add(keyCode);
            }
        } else {
            down.remove(keyCode);
        }
    }

    public synchronized InputSnapshot snapshot() {
        return new InputSnapshot(down, pressed, pointerPressed, pointerX, pointerY);
    }

    public synchronized void finishFrame() {
        pressed.clear();
        pointerPressed = false;
    }

    public synchronized void pressPointer(int x, int y) {
        pointerPressed = true;
        pointerX = x;
        pointerY = y;
    }
}

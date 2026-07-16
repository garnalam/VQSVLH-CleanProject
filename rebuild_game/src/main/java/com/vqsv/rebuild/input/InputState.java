package com.vqsv.rebuild.input;

import java.util.HashSet;
import java.util.Set;

public final class InputState {
    private final Set<Integer> down = new HashSet<>();
    private final Set<Integer> pressed = new HashSet<>();
    private boolean pointerPressed;
    private boolean pointerMoved;
    private int pointerX = -1;
    private int pointerY = -1;
    private int wheelRotation;

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
        return new InputSnapshot(down, pressed, pointerPressed, pointerMoved,
                pointerX, pointerY, wheelRotation);
    }

    public synchronized void finishFrame() {
        pressed.clear();
        pointerPressed = false;
        pointerMoved = false;
        wheelRotation = 0;
    }

    public synchronized void pressPointer(int x, int y) {
        pointerPressed = true;
        pointerX = x;
        pointerY = y;
    }

    public synchronized void movePointer(int x, int y) {
        pointerMoved = true;
        pointerX = x;
        pointerY = y;
    }

    public synchronized void rotateWheel(int rotation) {
        wheelRotation += rotation;
    }
}

package com.vqsv.rebuild.state;

import com.vqsv.rebuild.input.InputSnapshot;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class LegacyIntroDemoState implements GameState {
    private final Object scene;
    private final Method tick;
    private final Method render;
    private final Method press0;
    private final Method setMoveKey;
    private final Method click;

    public LegacyIntroDemoState() {
        this(false);
    }

    public LegacyIntroDemoState(boolean loadSave) {
        try {
            Class<?> sceneClass = Class.forName("VqsvIntroDemo$Scene");
            Constructor<?> constructor = sceneClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.scene = constructor.newInstance();
            if (loadSave) {
                Class<?> saveClass = Class.forName("VqsvSaveRuntime");
                Method loadInto = saveClass.getDeclaredMethod("loadInto", sceneClass);
                loadInto.setAccessible(true);
                Boolean loaded = (Boolean) loadInto.invoke(null, this.scene);
                if (!loaded.booleanValue()) {
                    throw new IllegalStateException("No compatible VQSV rebuild save found");
                }
            }
            this.tick = sceneClass.getDeclaredMethod("tick");
            this.render = sceneClass.getDeclaredMethod("render", Graphics2D.class);
            this.press0 = sceneClass.getDeclaredMethod("press0");
            this.setMoveKey = sceneClass.getDeclaredMethod("setMoveKey", int.class, boolean.class);
            this.click = sceneClass.getDeclaredMethod("click", int.class, int.class);
            this.tick.setAccessible(true);
            this.render.setAccessible(true);
            this.press0.setAccessible(true);
            this.setMoveKey.setAccessible(true);
            this.click.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot start legacy scene_0 intro runner", exception);
        }
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        try {
            syncMoveKey(input, KeyEvent.VK_UP);
            syncMoveKey(input, KeyEvent.VK_DOWN);
            syncMoveKey(input, KeyEvent.VK_LEFT);
            syncMoveKey(input, KeyEvent.VK_RIGHT);
            syncMoveKey(input, KeyEvent.VK_W);
            syncMoveKey(input, KeyEvent.VK_A);
            syncMoveKey(input, KeyEvent.VK_S);
            syncMoveKey(input, KeyEvent.VK_D);
            syncMoveKey(input, KeyEvent.VK_NUMPAD8);
            syncMoveKey(input, KeyEvent.VK_NUMPAD2);
            syncMoveKey(input, KeyEvent.VK_NUMPAD4);
            syncMoveKey(input, KeyEvent.VK_NUMPAD6);
            syncMoveKey(input, KeyEvent.VK_ESCAPE);
            syncMoveKey(input, KeyEvent.VK_BACK_SPACE);
            if (input.pointerPressed()) {
                click.invoke(scene, input.pointerX() * VqsvIntroDemoScale.SCALE,
                        input.pointerY() * VqsvIntroDemoScale.SCALE);
            } else if (input.confirmPressed() || input.wasPressed(KeyEvent.VK_5)
                    || input.wasPressed(KeyEvent.VK_NUMPAD5)) {
                press0.invoke(scene);
            }
            tick.invoke(scene);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Legacy scene_0 tick failed", exception);
        }
    }

    private void syncMoveKey(InputSnapshot input, int keyCode) throws ReflectiveOperationException {
        setMoveKey.invoke(scene, keyCode, input.isDown(keyCode));
    }

    @Override
    public void render(Graphics2D graphics) {
        try {
            render.invoke(scene, graphics);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Legacy scene_0 render failed", exception);
        }
    }

    private static final class VqsvIntroDemoScale {
        static final int SCALE = 2;
    }
}

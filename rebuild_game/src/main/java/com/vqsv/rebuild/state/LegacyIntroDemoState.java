package com.vqsv.rebuild.state;

import com.vqsv.rebuild.input.InputSnapshot;

import java.awt.Graphics2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class LegacyIntroDemoState implements GameState {
    private final Object scene;
    private final Method tick;
    private final Method render;
    private final Method press0;

    public LegacyIntroDemoState() {
        try {
            Class<?> sceneClass = Class.forName("VqsvIntroDemo$Scene");
            Constructor<?> constructor = sceneClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            this.scene = constructor.newInstance();
            this.tick = sceneClass.getDeclaredMethod("tick");
            this.render = sceneClass.getDeclaredMethod("render", Graphics2D.class);
            this.press0 = sceneClass.getDeclaredMethod("press0");
            this.tick.setAccessible(true);
            this.render.setAccessible(true);
            this.press0.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot start legacy scene_0 intro runner", exception);
        }
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        try {
            if (input.confirmPressed() || input.pointerPressed()) {
                press0.invoke(scene);
            }
            tick.invoke(scene);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Legacy scene_0 tick failed", exception);
        }
    }

    @Override
    public void render(Graphics2D graphics) {
        try {
            render.invoke(scene, graphics);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Legacy scene_0 render failed", exception);
        }
    }
}

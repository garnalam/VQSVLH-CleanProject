package com.vqsv.rebuild.state;

import com.vqsv.rebuild.debug.VqsvDebugLog;
import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.input.InputSnapshot;
import com.vqsv.rebuild.resource.AssetPaths;

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
    private final Method debugSnapshot;
    private final Method consumePanelTitleResetRequest;
    private int debugTickCounter;

    public LegacyIntroDemoState() {
        this(false);
    }

    public LegacyIntroDemoState(boolean loadSave) {
        this(loadSave, false);
    }

    public LegacyIntroDemoState(boolean loadSave, boolean skipIntro) {
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
            } else if (skipIntro) {
                Method skipIntroMethod = sceneClass.getDeclaredMethod("skipIntroToTenYearsLaterForRelease");
                skipIntroMethod.setAccessible(true);
                skipIntroMethod.invoke(this.scene);
            }
            this.tick = sceneClass.getDeclaredMethod("tick");
            this.render = sceneClass.getDeclaredMethod("render", Graphics2D.class);
            this.press0 = sceneClass.getDeclaredMethod("press0");
            this.setMoveKey = sceneClass.getDeclaredMethod("setMoveKey", int.class, boolean.class);
            this.click = sceneClass.getDeclaredMethod("click", int.class, int.class);
            this.debugSnapshot = sceneClass.getDeclaredMethod("debugSnapshotForRelease");
            this.consumePanelTitleResetRequest = sceneClass.getDeclaredMethod("consumePanelTitleResetRequestForRelease");
            this.tick.setAccessible(true);
            this.render.setAccessible(true);
            this.press0.setAccessible(true);
            this.setMoveKey.setAccessible(true);
            this.click.setAccessible(true);
            this.debugSnapshot.setAccessible(true);
            this.consumePanelTitleResetRequest.setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot start legacy scene_0 intro runner", exception);
        }
    }

    public int sceneEventIndexForSmoke() {
        try {
            java.lang.reflect.Field eventIndex = scene.getClass().getDeclaredField("eventIndex");
            eventIndex.setAccessible(true);
            return eventIndex.getInt(scene);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot inspect legacy scene event index", exception);
        }
    }

    public String sceneTextForSmoke() {
        try {
            java.lang.reflect.Field text = scene.getClass().getDeclaredField("text");
            text.setAccessible(true);
            Object textBox = text.get(scene);
            if (textBox == null) {
                return null;
            }
            java.lang.reflect.Field textValue = textBox.getClass().getDeclaredField("text");
            textValue.setAccessible(true);
            return (String) textValue.get(textBox);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot inspect legacy scene text", exception);
        }
    }

    public int sceneIntFieldForSmoke(String fieldName) {
        try {
            java.lang.reflect.Field field = scene.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(scene);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot inspect legacy scene field " + fieldName, exception);
        }
    }

    public String sceneDebugSnapshotForSmoke() {
        try {
            return (String) debugSnapshot.invoke(scene);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Cannot inspect legacy scene debug snapshot", exception);
        }
    }

    public Object sceneForSmoke() {
        return scene;
    }

    @Override
    public void tick(InputSnapshot input, GameStateMachine states) {
        try {
            debugTickCounter++;
            boolean hasInput = input.isDown(KeyEvent.VK_UP) || input.isDown(KeyEvent.VK_DOWN)
                    || input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_RIGHT)
                    || input.isDown(KeyEvent.VK_W) || input.isDown(KeyEvent.VK_A)
                    || input.isDown(KeyEvent.VK_S) || input.isDown(KeyEvent.VK_D)
                    || input.isDown(KeyEvent.VK_NUMPAD8) || input.isDown(KeyEvent.VK_NUMPAD2)
                    || input.isDown(KeyEvent.VK_NUMPAD4) || input.isDown(KeyEvent.VK_NUMPAD6)
                    || input.confirmPressed() || input.backPressed() || input.pointerPressed();
            if (hasInput || debugTickCounter % 30 == 0) {
                VqsvDebugLog.log("legacy tick input="
                        + " U:" + (input.isDown(KeyEvent.VK_UP) || input.isDown(KeyEvent.VK_W)
                        || input.isDown(KeyEvent.VK_NUMPAD8))
                        + " D:" + (input.isDown(KeyEvent.VK_DOWN) || input.isDown(KeyEvent.VK_S)
                        || input.isDown(KeyEvent.VK_NUMPAD2))
                        + " L:" + (input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_A)
                        || input.isDown(KeyEvent.VK_NUMPAD4))
                        + " R:" + (input.isDown(KeyEvent.VK_RIGHT) || input.isDown(KeyEvent.VK_D)
                        || input.isDown(KeyEvent.VK_NUMPAD6))
                        + " confirm:" + input.confirmPressed()
                        + " back:" + input.backPressed()
                        + " pointer:" + input.pointerPressed()
                        + " before=" + debugSnapshot.invoke(scene));
            }
            setMoveKey.invoke(scene, KeyEvent.VK_UP,
                    input.isDown(KeyEvent.VK_UP) || input.isDown(KeyEvent.VK_W)
                            || input.isDown(KeyEvent.VK_NUMPAD8));
            setMoveKey.invoke(scene, KeyEvent.VK_DOWN,
                    input.isDown(KeyEvent.VK_DOWN) || input.isDown(KeyEvent.VK_S)
                            || input.isDown(KeyEvent.VK_NUMPAD2));
            setMoveKey.invoke(scene, KeyEvent.VK_LEFT,
                    input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_A)
                            || input.isDown(KeyEvent.VK_NUMPAD4));
            setMoveKey.invoke(scene, KeyEvent.VK_RIGHT,
                    input.isDown(KeyEvent.VK_RIGHT) || input.isDown(KeyEvent.VK_D)
                            || input.isDown(KeyEvent.VK_NUMPAD6));
            setMoveKey.invoke(scene, KeyEvent.VK_ESCAPE,
                    input.isDown(KeyEvent.VK_ESCAPE) || input.isDown(KeyEvent.VK_BACK_SPACE));
            if (input.pointerPressed()) {
                click.invoke(scene, input.pointerX() * VqsvIntroDemoScale.SCALE,
                        input.pointerY() * VqsvIntroDemoScale.SCALE);
            } else if (input.confirmPressed() || input.wasPressed(KeyEvent.VK_5)
                    || input.wasPressed(KeyEvent.VK_NUMPAD5)) {
                press0.invoke(scene);
            }
            tick.invoke(scene);
            if (((Boolean) consumePanelTitleResetRequest.invoke(scene)).booleanValue()) {
                VqsvDebugLog.log("legacy panel title reset -> BootFlowState"
                        + " source=game.h.n option.ui c=0 game.i.a(7)");
                states.replace(new BootFlowState(AssetPaths.fromWorkingTree(GameConfig.defaultConfig())));
                return;
            }
            if (hasInput || debugTickCounter % 30 == 0) {
                VqsvDebugLog.log("legacy tick after=" + debugSnapshot.invoke(scene));
            }
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

    private static final class VqsvIntroDemoScale {
        static final int SCALE = 2;
    }
}

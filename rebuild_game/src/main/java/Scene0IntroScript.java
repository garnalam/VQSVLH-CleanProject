import java.util.List;

final class Scene0IntroScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene0_intro",
            "modules/event/decoded/data__event__scene_0.mid.json",
            "scene_0 full intro chain through room1 village destruction block",
            "PORTED_MANUAL; source-backed but still manual",
            "Scene0IntroScript.appendTo",
            "Do not touch timing/images unless comparing against original.",
            "Good baseline confirmed by user before script split work."
    );

    private Scene0IntroScript() {
    }

    static void appendTo(List<Event> e) {
            e.add(s -> {
                s.effect.startSolid(0);
                s.text = TextBox.full(30, 90, VqsvText.Scene0Intro.TEXT[0], true);
                return sc -> {
                    if (sc.text != null && sc.text.readyForKey && sc.key0) {
                        sc.text.disposed = true;
                        sc.effect.clearOverlay();
                        return true;
                    }
                    return false;
                };
            });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 10); return s.effect::doneBars; });
            e.add(s -> { s.effect.startParticles(80); return null; });
            e.add(s -> { s.setCameraCenter(190, 0); return null; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{1, 0}, new int[]{1, 3}); return null; });
            e.add(s -> new Move(new int[]{0, 1}, new int[]{-26, 23}, new int[]{0, 0}, new int[]{5, 5}, new int[]{0, 0}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{3, -3}, new int[]{0, 0}, new int[]{20, 20}));
            e.add(s -> new Move(new int[]{0, 1}, new int[]{0, 0}, new int[]{-3, 3}, new int[]{0, 0}, new int[]{10, 10}));
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{18, 19, 20, 22, 23, 24}, new int[]{1, 3, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{18, 19, 20, 22, 23, 24}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{26, 27, 28, 14, 15, 16}, new int[]{1, 1, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{26, 27, 28, 14, 15, 16}); return null; });
            e.add(s -> new Path(new int[]{0, 1}, new int[][]{{257, 262, 249}, {125, 120, 133}}, new int[][]{{193, 188, 195}, {248, 253, 247}}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{32, 33, 35}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 160, 25); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{32, 33, 35}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{75}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> new Path(new int[]{1}, new int[][]{{185}}, new int[][]{{200}}));
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{4}, new int[]{0}, new int[]{6}));
            e.add(s -> { s.effect.startCircle(0, 1, 120, 160, 160); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startBars(13, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[1], false);
                return null;
            });
            e.add(s -> new Move(new int[]{1}, new int[]{0}, new int[]{-4}, new int[]{0}, new int[]{18}));
            e.add(s -> new Delay(50));
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{2, 3, 4}, new int[]{1, 1, 3}); return null; });
            e.add(s -> new Move(new int[]{2}, new int[]{35}, new int[]{0}, new int[]{2}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{4}, new int[]{-30}, new int[]{0}, new int[]{4}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{3}, new int[]{0}, new int[]{-32}, new int[]{0}, new int[]{5}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{1, 2, 3, 4}); return null; });
            e.add(s -> new Move(new int[]{0}, new int[]{-100}, new int[]{-14}, new int[]{2}, new int[]{2}));
            e.add(s -> { s.effect.startParticles(90); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[2], false);
                return null;
            });
            e.add(s -> new Delay(60));
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{5, 6, 7, 8}, new int[]{1, 1, 1, 1}); return null; });
            e.add(s -> new Move(new int[]{7}, new int[]{30}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{8}, new int[]{-38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{6}, new int[]{-33}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Move(new int[]{5}, new int[]{38}, new int[]{0}, new int[]{3}, new int[]{0}));
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> new Delay(5));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{5, 6, 7, 8}); return null; });
            e.add(s -> { s.loadScene7Room2(296, 140); return null; });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[3], false);
                return null;
            });
            e.add(s -> new Delay(15));
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{32, 33}, new int[]{1, 3}); return null; });
            e.add(s -> new Move(new int[]{32, 33}, new int[]{10, -10}, new int[]{0, 0}, new int[]{13, 12}, new int[]{0, 0}));
            e.add(s -> new Move(new int[]{32, 33}, new int[]{0, 0}, new int[]{5, -5}, new int[]{0, 0}, new int[]{18, 30}));
            e.add(s -> new Delay(15));
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{36, 37, 38}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{36, 37, 38}); return null; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{39, 40}, new int[]{1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{39, 40}); return null; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{41, 42, 43, 44, 45}, new int[]{1, 1, 1, 1, 1}); return null; });
            e.add(s -> new Delay(30));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{41, 42, 43, 44, 45}); return null; });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{57, 58, 59}, new int[]{1, 1, 1}); return null; });
            e.add(s -> new Delay(15));
            e.add(s -> { s.effect.startCircle(0, 0, 120, 160, 25); return s.effect::doneOverlay; });
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{57, 58, 59}); return null; });
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{32, 33}); return null; });
            e.add(s -> { s.effect.stopParticles(); return null; });
            e.add(s -> { s.effect.startCircle(0, 1, 120, 160, 160); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[4], false);
                return null;
            });
            e.add(s -> new Delay(50));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(95, 280, 240, 320);
                s.markWorldTransition(0, 0, 0);
                s.reloadBlankRoomCenteredOnActor(9);
                return null;
            });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{9}, new int[]{1}); return null; });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[5], false);
                return null;
            });
            e.add(s -> new Delay(140));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[6], false);
                return null;
            });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { s.effect.startParticles(80); return null; });
            e.add(s -> new Move(new int[]{9}, new int[]{0}, new int[]{-2}, new int[]{0}, new int[]{52}));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(340, 412, 240, 320);
                s.markWorldTransition(5, 3, 36);
                s.loadScene5Room3(340, 412);
                return null;
            });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}); return null; });
            e.add(s -> { s.effect.startFlash(1, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[7], false);
                return null;
            });
            e.add(s -> new CameraPanPoint(340, 412, 2));
            e.add(s -> new Delay(200));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[8], false);
                return null;
            });
            e.add(s -> new Delay(110));
            e.add(s -> {
                s.text = TextBox.box(10, 270, 220, 50, VqsvText.Scene0Intro.TEXT[9], false);
                return null;
            });
            e.add(s -> new Delay(35));
            e.add(s -> {
                s.text = TextBox.full(30, 90, VqsvText.Scene0Intro.TEXT[10], true);
                return VqsvSceneScriptSupport.waitForText();
            });
            e.add(s -> { s.effect.startBars(12, 1, 1, 240, 10, 50); return s.effect::doneBars; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.loadRoom1(340, 412);
                s.text = TextBox.full(30, 90, VqsvText.Scene0Intro.TEXT[11], true);
                return sc -> {
                    if (sc.text != null && sc.text.readyForKey && sc.key0) {
                        sc.text.disposed = true;
                        sc.effect.clearOverlay();
                        return true;
                    }
                    return false;
                };
            });
            e.add(s -> { s.effect.startFireParticles(100); return null; });
            e.add(s -> new Delay(8));
            e.add(s -> { VqsvSceneScriptSupport.setActive(s, new int[]{29, 28, 30}, new int[]{1, 1, 3}); return null; });
            e.add(s -> { s.followActor(30); return null; });
            e.add(s -> new Move(new int[]{30}, new int[]{-5}, new int[]{1}, new int[]{84}, new int[]{84}));
            e.add(s -> { VqsvSceneScriptSupport.hide(s, new int[]{28}); return null; });
            e.add(s -> new Move(new int[]{30, 29}, new int[]{-5, -5}, new int[]{-2, -2}, new int[]{50, 50}, new int[]{50, 50}));
            e.add(s -> { s.stopCameraFollow(); return null; });
            e.add(s -> { s.effect.stopParticles(); return null; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(199, 79, 240, 320);
                s.markWorldTransition(1, 3, 0);
                s.loadScene1Room3Entry(199, 79);
                return null;
            });
    }

}

import java.util.List;

final class Scene1Room0Group0Script {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group0",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group0 records 0..29",
            "PORTED_MANUAL_WITH_APPROX; task/reward side effects partly source-backed",
            "Scene1Room0Group0Script.appendTo",
            "Starts ten-years-later village tutorial flow.",
            "Hands off to room1 Bunny flow through op10/op23/op13-style manual sequence."
    );

    private Scene1Room0Group0Script() {
    }

    static void appendTo(List<Event> e) {
            // scene_1 room0 group0, records 0..29. Gameplay/task side effects remain approximate.
            VqsvIntroDemo.Scene.tenYearsEventIndex = e.size();
            e.add(s -> {
                s.text = TextBox.full(60, 90, VqsvText.Scene1Room0Group0.TEN_YEARS_TITLE, true);
                return VqsvSceneScriptSupport.waitForText();
            });
            e.add(s -> { VqsvSceneScriptSupport.setActive(s,
                    new int[]{36, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51},
                    new int[]{1, 1, 1, 1, 1, 1, 0, 1, 0, 3, 1, 0, 0, 0, 3}); return null; });
            e.add(s -> { s.setPlayerPositionApprox(199, 218); s.player.direction = 2; return null; });
            e.add(s -> new Delay(30));
            e.add(s -> {
                s.text = TextBox.box(10, 260, 220, 50, VqsvText.Scene1Room0Group0.NOISE, false);
                return null;
            });
            e.add(s -> new Delay(60));
            e.add(s -> { s.spawnActorEffect(36, 13); return null; });
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.ALI, VqsvText.Scene1Room0Group0.ALI_TALENT));
            e.add(s -> { s.spawnActorEffect(50, 13); return null; });
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.TITAN, VqsvText.Scene1Room0Group0.TITAN_REPLY));
            e.add(s -> { s.spawnActorEffect(36, 13); return null; });
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.ALI, VqsvText.Scene1Room0Group0.ALI_MOTIVE));
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_HO));
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_EXAM));
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.NEIL, VqsvText.Scene1Room0Group0.NEIL_READY));
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.ELDER, VqsvText.Scene1Room0Group0.ELDER_BUNNY_TASK));
            e.add(s -> s.op17Item(0, 0, 1));
            e.add(s -> s.op17Item(0, 1, 2));
            e.add(s -> s.op17Item(0, 4, 5));
            e.add(s -> { s.op39RefreshPets(); return null; });
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room0Group0.NEIL, VqsvText.Scene1Room0Group0.NEIL_SIMPLE));
            e.add(VqsvSceneScriptSupport.taskNotice(VqsvText.Scene1Room0Group0.TASK_BUNNY));
            e.add(s -> s.op10PlayerTimedAction(1, 4, 36));            e.add(s -> s.op10PlayerTimedAction(0, 4, 12));
            e.add(s -> s.op10PlayerTimedAction(1, 4, 8));
            e.add(s -> {
                s.prepareTransition(55, 279, 240, 320);
                s.op25SetGameFlag(1);
                s.markWorldTransition(1, 1, 37);
                s.loadScene1Room1(s.transitionCenterX, s.transitionCenterY);
                s.placePlayerAtTransitionActorApprox(37, 16);
                return null;
            });
            e.add(s -> new VqsvRoom1Group1SavePromptWrapper());
            VqsvIntroDemo.Scene.room1BunnyOp13EventIndex = e.size();
            e.add(s -> VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13.wrap(
                    new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32)));
    }

}

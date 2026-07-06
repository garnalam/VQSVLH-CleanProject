import java.util.List;

final class Scene1Room1BunnyScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room1_bunny",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room1 group0 records after op13 trigger",
            "PORTED_MANUAL_WITH_STUB; Bunny battle remains SourceBattleRuntime stub",
            "Scene1Room1BunnyScript.appendTo",
            "op13 trigger now lives in VqsvScriptBlocks.",
            "After battle, source path marks room1 group0 complete and waits transition back to room0."
    );

    private Scene1Room1BunnyScript() {
    }

    static void appendTo(List<Event> e) {
            // scene_1 room1 group0, records 1..10 after op13 trigger. Battle/capture remains a source-backed stub.
            e.add(VqsvBattleScripts::room1BunnyBattleCaptureRuntime);
            e.add(VqsvSceneScriptSupport.dialog(VqsvText.Scene1Room1Group0.NEIL, VqsvText.Scene1Room1Group0.BUNNY_REPORT));
            e.add(s -> { s.op56ActorVisibility(1, new int[]{50}, new int[]{0}); return null; });
            e.add(s -> { s.op23MarkEventComplete(1, 0, 1); return null; });
            e.add(VqsvSceneScriptSupport.taskNotice(VqsvText.Scene1Room1Group0.TASK_RETURN_ELDER));
            e.add(s -> {
                s.op14CompleteEvent(1, 1, 0);
                s.sourceStateTrace.add("PORTED op86 gate preview [1,1,0]="
                        + s.sourceEventState(1, 1, 0)
                        + " complete=" + s.sourceEventStateComplete(1, 1, 0));
                return new ActorTransitionFreeWorldTrigger(1, 1, 37, 3, 1, 0, 30);
            });
    }

}

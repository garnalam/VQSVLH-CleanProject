import java.util.List;

final class Scene1Room0Group2ElderScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group2_elder",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group2 records 0..15",
            "PORTED_MANUAL; starts only after actor52 op16 interaction",
            "Scene1Room0Group2ElderScript.appendTo",
            "Gate is op86 [1,1,0], then op16 actor 52 interaction.",
            "Actor interaction trigger lives in VqsvScriptBlocks."
    );

    private Scene1Room0Group2ElderScript() {
    }

    static void appendTo(List<Event> e) {
            // scene_1 room0 group2, records 0..15. Starts only after op16 actor 52 interaction.
            e.add(s -> new ActorInteractionFreeWorldTrigger(1, 0, 2, 1, 1, 0, 52));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.CAUGHT));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_BUNNY_CUTE, 1));
            e.add(s -> { s.op5ActorEffect(0, 0, 9, 0, 0); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_WRONG_TARGET));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_PET_OFFER, 1));
            e.add(s -> { s.op5ActorEffect(0, 0, 14, 0, 0); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_GO_SEE));
            e.add(dialog(VqsvText.Scene1Room0Group2.ELDER, VqsvText.Scene1Room0Group2.ELDER_ONLY_ONE, 1));
            e.add(dialog(VqsvText.Scene1Room0Group2.NEIL, VqsvText.Scene1Room0Group2.NEIL_NOT_FREE));
            e.add(s -> { s.op5ActorEffect(1, 52, 3, 0, 0); return null; });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group2 op45 taskFlag=1");
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group2.TASK_PET_CHOICE);
                return waitForText();
            });
            e.add(s -> { s.op14CompleteEvent(1, 0, 2); return null; });
    }

    private static Event dialog(String speaker, String text) {
        return VqsvIntroDemo.Scene.dialog(speaker, text);
    }

    private static Event dialog(String speaker, String text, int mode) {
        return VqsvIntroDemo.Scene.dialog(speaker, text, mode);
    }

    private static Blocking waitForText() {
        return VqsvIntroDemo.Scene.waitForText();
    }
}

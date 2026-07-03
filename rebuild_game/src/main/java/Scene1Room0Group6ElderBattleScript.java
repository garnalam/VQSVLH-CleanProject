import java.util.List;

final class Scene1Room0Group6ElderBattleScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group6_elder_battle",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group6 elder battle/reward/free-world unlock",
            "PORTED_MANUAL_WITH_STUB; elder battle remains SourceBattleRuntime stub",
            "Scene1Room0Group6ElderBattleScript.appendTo",
            "Covers op15, op8, op7, op9, dialog, op67, op37, op32, op47, reward/free-world state.",
            "Full game.d battle engine is still pending."
    );

    private Scene1Room0Group6ElderBattleScript() {
    }

    static void appendTo(List<Event> e) {
            // scene_1 room0 group6, records 0..21. Battle remains a controlled game.d stub.
            e.add(s -> new VqsvIntroDemo.Room0Group6Start());
            e.add(dialog(VqsvText.Scene1Room0Group6.ELDER, VqsvText.Scene1Room0Group6.ELDER_ATTACK, 1));
            e.add(s -> { s.op67SetBattleActor(52); return null; });
            e.add(s -> s.room0Group6ElderBattleRuntime());
            e.add(dialog(VqsvText.Scene1Room0Group6.ELDER, VqsvText.Scene1Room0Group6.ELDER_REWARD, 1));
            e.add(s -> s.op31CurrencyReward(0, 0, 500));
            e.add(s -> s.op17Item(0, 4, 10));
            e.add(s -> s.op17Item(0, 11, 2));
            e.add(s -> s.op19SpecialReward(5, 1));
            e.add(dialog(VqsvText.Scene1Room0Group6.ELDER, VqsvText.Scene1Room0Group6.ELDER_BOOK, 1));
            e.add(dialog(VqsvText.Scene1Room0Group6.ELDER, VqsvText.Scene1Room0Group6.ELDER_ABRA, 1));
            e.add(dialog(VqsvText.Scene1Room0Group6.NEIL, VqsvText.Scene1Room0Group6.NEIL_REMEMBER));
            e.add(s -> { s.op23MarkEventComplete(1, 0, 4); return null; });
            e.add(s -> { s.op23MarkEventComplete(1, 0, 5); return null; });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op45 taskFlag=2");
                s.text = TextBox.taskTip(VqsvText.Scene1Room0Group6.TASK_BICH_THUY);
                return waitForText();
            });
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op40 free-world notice");
                s.text = TextBox.openBox(VqsvText.Scene1Room0Group6.FREE_WORLD);
                return waitForText();
            });
            e.add(s -> { s.op14CompleteEvent(1, 0, 6); return null; });
            e.add(s -> new VqsvIntroDemo.Room0PostGroup6FreeWorld());
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

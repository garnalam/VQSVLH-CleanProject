import java.util.List;

final class Scene1Room3EntryScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room3_entry",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room3 group0 entry before transition to room0",
            "PORTED_MANUAL_WITH_APPROX; movement/effects remain source-guided manual",
            "Scene1Room3EntryScript.appendTo",
            "Includes six-years-later text, Neil/Sophie sequence, kidnapping battle stub.",
            "Battle is not full game.d yet."
    );

    private Scene1Room3EntryScript() {
    }

    static void appendTo(List<Event> e) {
            e.add(s -> {
                s.text = TextBox.full(60, 90, VqsvText.Scene1Room3BeforeTenYears.TEXT[0], true);
                return waitForText();
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
            e.add(s -> { setActive(s, new int[]{48, 49, 50}, new int[]{1, 2, 2}); return null; });
            e.add(s -> new CameraPan(49, 0));
            e.add(s -> new Delay(15));
            e.add(s -> new CameraPan(48, 10));
            e.add(dialog(VqsvText.Scene1Room0Group0.NEIL, VqsvText.Scene1Room3BeforeTenYears.TEXT[1]));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{0, 0}, new int[]{4, 4}, new int[]{13, 13}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{3, 3}, new int[]{4, 4}, new int[]{13, 13}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{0, 0}, new int[]{4, 4}, new int[]{23, 23}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{3, 3}, new int[]{4, 4}, new int[]{20, 20}));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{2, 2}, new int[]{4, 4}, new int[]{18, 18}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{1, 1}, new int[]{3, 3}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{1, 1}, new int[]{1, 1}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{2, 2}, new int[]{2, 2}));
            e.add(s -> { s.spawnActorEffect(49, 14); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[2]));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{0}, new int[]{4}, new int[]{6}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{1}, new int[]{4}, new int[]{13}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{8}));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[3]));
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[4]));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[5]));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[6]));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{0, 0, 0}, new int[]{4, 4, 4}, new int[]{13, 13, 13}));
            e.add(s -> new TimedAction(new int[]{48, 49, 50}, new int[]{1, 1, 1}, new int[]{4, 4, 4}, new int[]{20, 20, 20}));
            e.add(s -> new ActionSet(new int[]{48}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[7]));
            e.add(s -> new ActionSet(new int[]{49}, new int[]{0}, new int[]{0}));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[8]));
            e.add(s -> { s.spawnActorEffect(48, 8); return null; });
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[9]));
            e.add(s -> new TimedAction(new int[]{49, 50}, new int[]{2, 2}, new int[]{4, 4}, new int[]{16, 16}));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[10]));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[11]));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[12]));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[13]));
            e.add(s -> new Delay(15));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{10}));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[14]));
            e.add(s -> { s.spawnActorEffect(49, 5); return null; });
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[15]));
            e.add(dialog("Neil", "..."));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[16]));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[17]));
            e.add(s -> { s.spawnActorEffect(49, 14); return null; });
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[18]));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[19]));
            e.add(s -> { setActive(s, new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}); return null; });
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{4, 4, 4, 4}, new int[]{23, 23, 23, 23}));
            e.add(s -> new TimedAction(new int[]{53, 54, 55, 56}, new int[]{3, 3, 3, 3}, new int[]{4, 4, 4, 4}, new int[]{15, 15, 15, 15}));
            e.add(s -> new ActionSet(new int[]{53, 54, 55, 56}, new int[]{0, 0, 0, 0}, new int[]{0, 0, 0, 0}));
            e.add(dialog("??", VqsvText.Scene1Room3BeforeTenYears.TEXT[20]));
            e.add(s -> { s.spawnActorEffect(48, 7); return null; });
            e.add(s -> { s.spawnActorEffect(49, 7); return null; });
            e.add(s -> new ActionSet(new int[]{49}, new int[]{2}, new int[]{2}));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[21]));
            e.add(s -> new ActionSet(new int[]{49, 50}, new int[]{0, 0}, new int[]{0, 0}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{0, 0, 0, 0}, new int[]{6, 4, 6, 4}, new int[]{4, 4, 4, 4}));
            e.add(s -> new TimedAction(new int[]{53, 49, 56, 50}, new int[]{2, 2, 2, 2}, new int[]{4, 4, 4, 4}, new int[]{6, 6, 6, 6}));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[22]));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{6}));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[23]));
            e.add(s -> new ActionSet(new int[]{53, 56, 49}, new int[]{0, 0, 0}, new int[]{0, 0, 0}));
            e.add(dialog("??", VqsvText.Scene1Room3BeforeTenYears.TEXT[24]));
            e.add(dialog("??", VqsvText.Scene1Room3BeforeTenYears.TEXT[25]));
            e.add(s -> new SourceBattleRuntime(
                    56,
                    new int[]{5, 20, 4},
                    new int[]{1, 1},
                    new int[]{0, 2},
                    new int[]{78, 78, 0}));
            e.add(s -> { hide(s, new int[]{50}); return null; });
            e.add(dialog("??", VqsvText.Scene1Room3BeforeTenYears.TEXT[26]));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[27]));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[28]));
            e.add(s -> { s.spawnActorEffect(49, 6); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Sophie", VqsvText.Scene1Room3BeforeTenYears.TEXT[29]));
            e.add(dialog("??", VqsvText.Scene1Room3BeforeTenYears.TEXT[30]));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{1, 1, 1, 1, 1}, new int[]{4, 4, 4, 4, 4}, new int[]{15, 15, 15, 15, 15}));
            e.add(s -> new TimedAction(new int[]{48}, new int[]{2}, new int[]{4}, new int[]{4}));
            e.add(s -> new TimedAction(new int[]{49, 53, 54, 55, 56}, new int[]{2, 2, 2, 2, 2}, new int[]{4, 4, 4, 4, 4}, new int[]{23, 23, 23, 23, 23}));
            e.add(s -> { hide(s, new int[]{49, 53, 54, 55, 56}); return null; });
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> new Opcode34Counter(70, 0, 0));
            e.add(s -> {
                s.text = TextBox.box(20, 220, 200, 40, VqsvText.Scene1Room3BeforeTenYears.TEXT[31], true);
                return waitForText();
            });
            e.add(s -> new Delay(30));
            e.add(s -> {
                s.text = TextBox.full(60, 90, VqsvText.Scene1Room3BeforeTenYears.TEXT[32], true);
                return waitForText();
            });
            e.add(s -> { s.spawnActorEffect(48, 1); return null; });
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[33]));
            e.add(s -> { s.effect.startIcon("ikon_1", 120, 100, 10); return s.effect::doneOverlay; });
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[34]));
            e.add(s -> { s.spawnActorEffect(48, 13); return null; });
            e.add(s -> new Delay(15));
            e.add(dialog("Neil", VqsvText.Scene1Room3BeforeTenYears.TEXT[35]));
            e.add(s -> { s.effect.startFade(2, 0); return s.effect::doneOverlay; });
            e.add(s -> {
                s.prepareTransition(199, 218, 240, 320, 2);
                s.markWorldTransition(1, 0, -1);
                return null;
            });
            e.add(s -> {
                s.loadScene1Room0(s.transitionCenterX, s.transitionCenterY);
                return null;
            });
            e.add(s -> { s.effect.startFade(1, 0); return s.effect::doneOverlay; });
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

    private static void setActive(VqsvIntroDemo.Scene s, int[] ids, int[] dirs) {
        VqsvIntroDemo.Scene.setActive(s, ids, dirs);
    }

    private static void hide(VqsvIntroDemo.Scene s, int[] ids) {
        VqsvIntroDemo.Scene.hide(s, ids);
    }
}

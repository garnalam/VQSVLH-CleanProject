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
            e.add(s -> new Room0Group6Start());
            e.add(VqsvSceneScriptSupport.dialogOp4(VqsvText.Scene1Room0Group6.ELDER,
                    VqsvText.Scene1Room0Group6.ELDER_ATTACK, 0, -1));
            e.add(s -> { s.op67SetBattleActor(52); return null; });
            e.add(VqsvBattleScripts::room0Group6ElderBattleRuntime);
            e.add(s -> { VqsvBattleEventDescriptor.SCENE1_ROOM0_GROUP6_ELDER.consumeOp47(s); return null; });
            e.add(VqsvSceneScriptSupport.dialogOp4(VqsvText.Scene1Room0Group6.ELDER,
                    VqsvText.Scene1Room0Group6.ELDER_REWARD, 0, -1));
            e.add(s -> s.op31CurrencyReward(0, 0, 500));
            e.add(s -> s.op17Item(0, 4, 10));
            e.add(s -> s.op17Item(0, 11, 2));
            e.add(s -> s.op19SpecialReward(5, 1));
            e.add(VqsvSceneScriptSupport.dialogOp4(VqsvText.Scene1Room0Group6.ELDER,
                    VqsvText.Scene1Room0Group6.ELDER_BOOK, 0, -1));
            e.add(VqsvSceneScriptSupport.dialogOp4(VqsvText.Scene1Room0Group6.ELDER,
                    VqsvText.Scene1Room0Group6.ELDER_ABRA, 0, -1));
            e.add(VqsvSceneScriptSupport.dialogOp4(VqsvText.Scene1Room0Group6.NEIL,
                    VqsvText.Scene1Room0Group6.NEIL_REMEMBER, 0, 0));
            e.add(s -> { s.op23MarkEventComplete(1, 0, 4); return null; });
            e.add(s -> { s.op23MarkEventComplete(1, 0, 5); return null; });
            e.add(VqsvSceneScriptSupport.taskNoticeOp45(2,
                    VqsvText.Scene1Room0Group6.TASK_BICH_THUY, "scene1 room0 group6"));
            e.add(s -> {
                s.sourceStateTrace.add("PORTED/APPROX room0 group6 op40 free-world notice");
                s.text = TextBox.openBox(VqsvText.Scene1Room0Group6.FREE_WORLD);
                return VqsvSceneScriptSupport.waitForText();
            });
            e.add(s -> {
                s.op14CompleteEvent(1, 0, 6);
                VqsvPostBattleDownstreamDescriptor.SCENE1_ROOM0_GROUP6_ELDER.traceAndAssert(s);
                return null;
            });
            e.add(s -> VqsvWorldResumeDescriptor.SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD.wrap(
                    new Room0PostGroup6FreeWorld()));
    }
}

final class Room0Group6Start implements Blocking {
    private int phase;
    private int wait;
    private Blocking effectWait;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!s.op15CheckEventState(1, 0, 3)) {
            s.tickFreeWorldPlayer();
            return false;
        }
        if (phase == 0) {
            s.sourceStateTrace.add("PORTED room0 group6 op15 [1,0,3] pass");
            s.setPlayerPositionApprox(199, 218);
            s.player.direction = 2;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op8 set player=[199,218]");
            s.player.applyMode(2);
            wait = 24;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 op7 actor=-1 state=0 action=2");
            phase = 1;
            return false;
        }
        if (phase == 1) {
            if (wait-- > 0) {
                return false;
            }
            s.player.applyMode(0);
            effectWait = s.op9SourceEffect("room0 group6", 1, 0, 0, 0, 0, 0);
            phase = 2;
            return false;
        }
        return effectWait == null || effectWait.tick(s);
    }
}

final class Room0PostGroup6FreeWorld implements Blocking {
    private boolean started;
    private boolean room2Group3Started;
    private boolean dodoPendingLogged;
    private boolean doorPendingLogged;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!started) {
            started = true;
            s.sourceStateTrace.add("PORTED/APPROX room0 group6 enters free-world after op40/op14");
        }
        if (s.currentSceneId == 1 && s.currentRoomIndex == 0) {
            return tickRoom0(s);
        }
        if (s.currentSceneId == 1 && s.currentRoomIndex == 2) {
            return tickRoom2(s);
        }
        s.tickFreeWorldPlayer();
        return false;
    }

    private boolean tickRoom0(VqsvIntroDemo.Scene s) {
        if (s.trySourceTransition(31, 2, 1, 2, 2)) {
            room2Group3Started = false;
            return false;
        }
        if (s.trySourceTransition(30, 3, 1, 1, 37)) {
            return false;
        }
        if (s.key0 && s.playerInteractsActorSourceMask(35) && !dodoPendingLogged) {
            dodoPendingLogged = true;
            s.sourceStateTrace.add("PENDING room0 post-group6 Dodo actor35 side quest groups 7/8/9 not ported yet");
        }
        if (!doorPendingLogged) {
            if (s.playerIntersectsActorSourceMask(3, true)
                    || s.playerIntersectsActorSourceMask(4, true)
                    || s.playerIntersectsActorSourceMask(5, true)) {
                doorPendingLogged = true;
                s.sourceStateTrace.add("PENDING room0 post-group6 scene11 door transitions actors=[3,4,5] not ported yet");
            }
        }
        s.tickFreeWorldPlayer();
        if (s.trySourceTransition(31, 2, 1, 2, 2)) {
            room2Group3Started = false;
            return false;
        }
        if (s.trySourceTransition(30, 3, 1, 1, 37)) {
            return false;
        }
        return false;
    }

    private boolean tickRoom2(VqsvIntroDemo.Scene s) {
        if (!s.op15CheckEventState(1, 2, 3) && !room2Group3Started) {
            room2Group3Started = true;
            s.sourceStateTrace.add("PORTED scene1 room2 group3 op15 [1,0,6] pass");
            s.text = TextBox.openBox(VqsvText.Common.MINIMAP_TASK_HELP);
            return false;
        }
        if (room2Group3Started) {
            if (s.text != null && s.text.readyForKey && s.key0) {
                s.text.confirm();
                return false;
            }
            if (s.text == null) {
                s.op14CompleteEvent(1, 2, 3);
                s.sourceStateTrace.add("PORTED scene1 room2 group3 op14 complete");
                room2Group3Started = false;
            }
            return false;
        }
        if (s.trySourceTransition(2, 0, 1, 0, 31)) {
            return false;
        }
        s.tickFreeWorldPlayer();
        if (s.trySourceTransition(2, 0, 1, 0, 31)) {
            return false;
        }
        if (s.trySourceTransition(3, 2, 1, 3, 24)) {
            s.sourceStateTrace.add("PENDING scene1 room2 actor3 to room3 target24 needs room3 free-world loader audit");
        }
        return false;
    }
}

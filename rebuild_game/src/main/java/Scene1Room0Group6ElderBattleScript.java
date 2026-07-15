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

final class Room0PostGroup6FreeWorld implements Blocking, SourceWorldPanelOpen {
    private boolean started;
    private boolean room2Group3Started;
    private boolean dodoPendingLogged;
    private boolean doorPendingLogged;
    private Blocking dodoEvent;

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
        if (dodoEvent != null) {
            if (dodoEvent.tick(s)) {
                dodoEvent = null;
            }
            return false;
        }
        if (s.trySourceTransition(31, 2, 1, 2, 2)) {
            room2Group3Started = false;
            return false;
        }
        if (s.trySourceTransition(30, 3, 1, 1, 37)) {
            return false;
        }
        if (s.key0 && s.playerInteractsActorSourceMask(35)) {
            if (!s.sourceEventStateComplete(1, 0, 7)
                    && s.sourceBranchTaskStatus(0) < 0) {
                dodoEvent = Scene1Room0Group7DodoScript.startFreeWorldInteraction(s);
                return false;
            }
            if (s.sourceBranchTaskStatus(0) == 1) {
                if (s.sourcePetRecordObtained(1, 23)) {
                    dodoEvent = new DodoGroup8CompletionFlow();
                    return false;
                }
                s.sourceRefreshBqTaskMarkers();
                s.sourceStateTrace.add("PORTED/PARTIAL scene1 room0 group8 opcode44 objective missing"
                        + " task0Status=1 source game.j.a(1,23)!=2");
                s.key0 = false;
                return false;
            }
            if (s.sourceBranchTaskStatus(0) == 3
                    && s.sourceEventStateComplete(1, 0, 8)
                    && !s.sourceEventStateComplete(1, 0, 9)
                    && s.sourceBranchTaskStatus(1) < 0) {
                dodoEvent = Scene1Room0Group9DodoScript.startFreeWorldInteraction(s);
                return false;
            }
            if (!dodoPendingLogged) {
                dodoPendingLogged = true;
                s.sourceStateTrace.add("PENDING room0 post-group6 Dodo actor35 side quest completion not ported yet"
                        + " task0Status=" + s.sourceBranchTaskStatus(0)
                        + " task1Status=" + s.sourceBranchTaskStatus(1));
            }
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

    private static final class DodoGroup8CompletionFlow implements Blocking {
        private int phase;
        private Blocking wait;

        @Override
        public boolean tick(VqsvIntroDemo.Scene s) {
            if (wait != null) {
                if (!wait.tick(s)) {
                    return false;
                }
                wait = null;
                phase++;
            }
            switch (phase) {
                case 0:
                    s.stopPlayerForSourceEvent();
                    s.worldEventActor = 35;
                    s.sourceStateTrace.add("PORTED scene1 room0 group8 opcode44 start"
                            + " short=[0,1,1,0,35,1,0,7,0,1,23,0]");
                    s.text = TextBox.dialog(s.font, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_COMPLETE_TASK0, -1, 0);
                    s.sourceStateTrace.add("PORTED op4 dialog.ui speaker=\"Dodo\""
                            + " group8 task0 complete thanks");
                    wait = VqsvSceneScriptSupport.waitForText();
                    return false;
                case 1:
                    wait = s.op17Item(0, 1, 3);
                    s.sourceStateTrace.add("PORTED scene1 room0 group8 op17 reward [0,1,3]");
                    return false;
                case 2:
                    s.sourceCompleteBranchTask(0);
                    s.op14CompleteEvent(1, 0, 8);
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED scene1 room0 group8 op14 complete"
                            + " and source game.e.m(0) status=3");
                    phase = 99;
                    return true;
                default:
                    return true;
            }
        }
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

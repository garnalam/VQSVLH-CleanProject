final class Scene2Room1Group6ElizaScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene2_room1_group6_eliza_branch_task1_complete",
            "modules/event/decoded/data__event__scene_2.mid.json",
            "room1 group6 Eliza branch task1 completion",
            "PORTED_PARTIAL; opcode44 completion for task1 only, no generic quest VM",
            "Scene2Room1Group6ElizaScript.startCompletion",
            "Covers opcode44 objective [task1, category4 species68], op4 dialogs, op17 reward [0,1,3], op14, game.e.m(1).",
            "Full scene2 room1 map/actor parity and generic bqTask predicates remain pending."
    );

    private Scene2Room1Group6ElizaScript() {
    }

    static Blocking startCompletion(VqsvIntroDemo.Scene s) {
        s.stopPlayerForSourceEvent();
        s.worldEventActor = 73;
        s.sourceStateTrace.add("PORTED scene2 room1 group6 opcode44 start"
                + " short=[1,1,2,1,73,1,0,9,0,4,68,0]");
        return new CompletionFlow();
    }

    private static final class CompletionFlow implements Blocking {
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
                    wait = startDialog(s, VqsvText.Scene2Room1Group6.ELIZA,
                            VqsvText.Scene2Room1Group6.ELIZA_ASK);
                    return false;
                case 1:
                    wait = startDialog(s, VqsvText.Scene2Room1Group6.ELIZA,
                            VqsvText.Scene2Room1Group6.ELIZA_DODO);
                    return false;
                case 2:
                    wait = startDialog(s, VqsvText.Scene2Room1Group6.ELIZA,
                            VqsvText.Scene2Room1Group6.ELIZA_THANKS);
                    return false;
                case 3:
                    wait = s.op17Item(0, 1, 3);
                    s.sourceStateTrace.add("PORTED scene2 room1 group6 op17 reward [0,1,3]");
                    return false;
                case 4:
                    s.sourceCompleteBranchTask(1);
                    s.op14CompleteEvent(2, 1, 6);
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED scene2 room1 group6 op14 complete"
                            + " and source game.e.m(1) status=3");
                    phase = 99;
                    return true;
                default:
                    return true;
            }
        }

        private static Blocking startDialog(VqsvIntroDemo.Scene s, String speaker, String text) {
            s.sourceStateTrace.add("PORTED op4 dialog.ui speaker=\"" + speaker
                    + "\" side=-1 portrait=0");
            s.text = TextBox.dialog(s.font, speaker, text, -1, 0);
            return VqsvSceneScriptSupport.waitForText();
        }
    }
}

final class Scene2Room1FreeWorld implements Blocking, SourceWorldPanelOpen {
    private Blocking elizaEvent;
    private boolean pendingLogged;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (elizaEvent != null) {
            if (elizaEvent.tick(s)) {
                elizaEvent = null;
            }
            return false;
        }
        if (s.key0 && s.playerInteractsActorSourceMask(73)) {
            if (s.sourceBranchTaskStatus(1) == 1) {
                if (s.sourcePetRecordObtained(4, 68)) {
                    elizaEvent = Scene2Room1Group6ElizaScript.startCompletion(s);
                    return false;
                }
                s.sourceRefreshBqTaskMarkers();
                s.sourceStateTrace.add("PORTED/PARTIAL scene2 room1 group6 opcode44 objective missing"
                        + " task1Status=1 source game.j.a(4,68)!=2");
                s.key0 = false;
                return false;
            }
            if (!pendingLogged) {
                pendingLogged = true;
                s.sourceStateTrace.add("PENDING scene2 room1 actor73 Eliza non-task dialog"
                        + " task1Status=" + s.sourceBranchTaskStatus(1));
            }
            s.key0 = false;
        }
        s.tickFreeWorldPlayer();
        return false;
    }
}

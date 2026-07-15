final class Scene1Room0Group9DodoScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group9_dodo_branch_task1",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group9 Dodo branch task1 accept/reject",
            "PORTED_PARTIAL; opcode49 accept writes source F/H for task1, completion remains pending",
            "Scene1Room0Group9DodoScript.startFreeWorldInteraction",
            "Covers op43 actor35 branch-task gate, op49 accept/reject, op40 tips, op14 complete.",
            "Does not port aq[1]=[2,1,6] completion/scene2 marker yet."
    );

    private Scene1Room0Group9DodoScript() {
    }

    static Blocking startFreeWorldInteraction(VqsvIntroDemo.Scene s) {
        s.stopPlayerForSourceEvent();
        s.worldEventActor = 35;
        s.sourceStateTrace.add("PORTED scene1 room0 group9 op43 actor35 branch task1 start"
                + " short=[1,1,1,0,35,1,0,8,1,0,0]");
        return new DodoGroup9Flow();
    }

    private static final class DodoGroup9Flow implements Blocking {
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
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_CALL);
                    return false;
                case 1:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_ASK);
                    return false;
                case 2:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_REQUEST_1);
                    return false;
                case 3:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_REQUEST_2);
                    return false;
                case 4:
                    s.panelRuntime.openBranchTaskAcceptOption(s, 1, false);
                    s.sourceStateTrace.add("PORTED scene1 room0 group9 op49 task1 option"
                            + " short=[0,1,-1,-1] options=7,11");
                    s.key0 = false;
                    s.keyBack = false;
                    s.keyDown = false;
                    s.keyUp = false;
                    phase = 5;
                    return false;
                case 5:
                    if (s.panelRuntime.visible && "TASK_OPTION".equals(s.panelRuntime.modeName())) {
                        return false;
                    }
                    if (s.sourceBranchTaskStatus(1) == 1) {
                        s.sourceStateTrace.add("PORTED scene1 room0 group9 op49 accept branch task1");
                        phase = 10;
                    } else {
                        s.sourceStateTrace.add("PORTED scene1 room0 group9 op49 reject/back branch task1");
                        phase = 20;
                    }
                    return false;
                case 10:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_ACCEPT);
                    return false;
                case 11:
                    wait = startTaskTip(s, VqsvText.Scene1Room0Group7.TASK_DIEN_MIEU);
                    return false;
                case 12:
                    wait = startTaskTip(s, VqsvText.Scene1Room0Group7.TASK_UPDATED);
                    return false;
                case 13:
                    s.op14CompleteEvent(1, 0, 9);
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED scene1 room0 group9 op14 complete after accept"
                            + " aq[1]=[2,1,6] completion marker pending");
                    phase = 99;
                    return true;
                case 20:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_TASK1_REJECT);
                    return false;
                case 21:
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED/PARTIAL scene1 room0 group9 reject closes via op42-equivalent");
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

        private static Blocking startTaskTip(VqsvIntroDemo.Scene s, String text) {
            s.sourceStateTrace.add("PORTED scene1 room0 group9 op40 taskTip.ui text=\"" + text + "\"");
            s.text = TextBox.taskTip(text);
            return VqsvSceneScriptSupport.waitForText();
        }
    }
}

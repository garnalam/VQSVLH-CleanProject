import java.util.List;

final class Scene1Room0Group7DodoScript {
    static final VqsvScripts.ScriptInfo INFO = new VqsvScripts.ScriptInfo(
            "scene1_room0_group7_dodo_branch_task0",
            "modules/event/decoded/data__event__scene_1.mid.json",
            "room0 group7 Dodo branch task0 accept/reject",
            "PORTED_PARTIAL; opcode49 accept writes source F/H, reward payload still taskOption.ui partial",
            "Scene1Room0Group7DodoScript.startFreeWorldInteraction",
            "Covers op43 actor35 branch-task gate, op49 accept/reject, op40 tips, op14 complete.",
            "Completion group8 is ported in Room0PostGroup6FreeWorld; group9 task1 remains pending."
    );

    private Scene1Room0Group7DodoScript() {
    }

    static void appendTo(List<Event> e) {
        e.add(Scene1Room0Group7DodoScript::startFreeWorldInteraction);
    }

    static Blocking startFreeWorldInteraction(VqsvIntroDemo.Scene s) {
        s.stopPlayerForSourceEvent();
        s.worldEventActor = 35;
        s.sourceStateTrace.add("PORTED scene1 room0 group7 op43 actor35 branch task0 start");
        return new DodoGroup7Flow();
    }

    private static final class DodoGroup7Flow implements Blocking {
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
                            VqsvText.Scene1Room0Group7.DODO_CALL);
                    return false;
                case 1:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.NEIL,
                            VqsvText.Scene1Room0Group7.NEIL_ASK);
                    return false;
                case 2:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_REQUEST);
                    return false;
                case 3:
                    s.panelRuntime.openBranchTaskAcceptOption(s, 0, false);
                    s.sourceStateTrace.add("PORTED scene1 room0 group7 op49 task0 option"
                            + " short=[0,1,-1,-1] options=6,10");
                    s.key0 = false;
                    s.keyBack = false;
                    s.keyDown = false;
                    s.keyUp = false;
                    phase = 4;
                    return false;
                case 4:
                    if (s.panelRuntime.visible && "TASK_OPTION".equals(s.panelRuntime.modeName())) {
                        return false;
                    }
                    if (s.sourceBranchTaskStatus(0) == 1) {
                        s.sourceStateTrace.add("PORTED scene1 room0 group7 op49 accept branch");
                        phase = 10;
                    } else {
                        s.sourceStateTrace.add("PORTED scene1 room0 group7 op49 reject/back branch");
                        phase = 20;
                    }
                    return false;
                case 10:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_ACCEPT);
                    return false;
                case 11:
                    wait = startTaskTip(s, VqsvText.Scene1Room0Group7.TASK_MOC_LINH);
                    return false;
                case 12:
                    wait = startTaskTip(s, VqsvText.Scene1Room0Group7.TASK_UPDATED);
                    return false;
                case 13:
                    s.op14CompleteEvent(1, 0, 7);
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED scene1 room0 group7 op14 complete after accept");
                    phase = 99;
                    return true;
                case 20:
                    wait = startDialog(s, VqsvText.Scene1Room0Group7.DODO,
                            VqsvText.Scene1Room0Group7.DODO_REJECT);
                    return false;
                case 21:
                    s.sourceRefreshBqTaskMarkers();
                    s.sourceStateTrace.add("PORTED/PARTIAL scene1 room0 group7 reject closes via op42-equivalent");
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
            s.sourceStateTrace.add("PORTED scene1 room0 group7 op40 taskTip.ui text=\"" + text + "\"");
            s.text = TextBox.taskTip(text);
            return VqsvSceneScriptSupport.waitForText();
        }
    }
}

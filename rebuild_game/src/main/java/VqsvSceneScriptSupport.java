final class VqsvSceneScriptSupport {
    private VqsvSceneScriptSupport() {
    }

    static void setActive(VqsvIntroDemo.Scene s, int[] ids, int[] dirs) {
        for (int i = 0; i < ids.length; i++) {
            Actor a = s.actors[ids[i]];
            if (a != null) {
                a.direction = dirs[i];
                a.applyMode(0);
                a.visible = true;
            }
        }
    }

    static void hide(VqsvIntroDemo.Scene s, int[] ids) {
        for (int id : ids) {
            if (s.actors[id] != null) {
                s.actors[id].visible = false;
            }
        }
    }

    static Event dialog(String speaker, String text) {
        return s -> {
            s.text = TextBox.dialog(s.font, speaker, text, 0);
            return waitForText();
        };
    }

    static Event dialog(String speaker, String text, int mode) {
        return s -> {
            s.text = TextBox.dialog(s.font, speaker, text, mode);
            return waitForText();
        };
    }

    static Event dialogOp4(String speaker, String text, int side, int portraitIndex) {
        return s -> {
            s.sourceStateTrace.add("PORTED op4 dialog.ui speaker=\"" + speaker
                    + "\" side=" + side + " portrait=" + portraitIndex);
            s.text = TextBox.dialog(s.font, speaker, text, side, portraitIndex);
            return waitForText();
        };
    }

    static Event taskNotice(String text) {
        return s -> {
            s.text = TextBox.taskTip(text);
            return waitForText();
        };
    }

    static Event taskNoticeOp45(int taskFlag, String text, String routeLabel) {
        return s -> {
            s.sourceSetMainTaskProgress(taskFlag, "op45 taskTip.ui route=" + routeLabel);
            s.sourceStateTrace.add("PORTED op45 taskTip.ui route=" + routeLabel
                    + " taskFlag=" + taskFlag + " text=\"" + text + "\"");
            s.text = TextBox.taskTip(text);
            return waitForText();
        };
    }

    static Event branchTaskAcceptOp49(int taskId, String routeLabel) {
        return s -> {
            s.panelRuntime.openBranchTaskAcceptOption(s, taskId, false);
            s.sourceStateTrace.add("PORTED/PARTIAL op49 branch task option route="
                    + routeLabel + " taskId=" + taskId);
            return sc -> !sc.panelRuntime.visible
                    || !"TASK_OPTION".equals(sc.panelRuntime.modeName());
        };
    }

    static Blocking waitForText() {
        return sc -> {
            if (sc.text != null && sc.text.readyForKey && sc.key0) {
                return sc.text.confirm();
            }
            return false;
        };
    }
}

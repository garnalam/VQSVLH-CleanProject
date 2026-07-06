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

    static Event taskNotice(String text) {
        return s -> {
            s.text = TextBox.taskTip(text);
            return waitForText();
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

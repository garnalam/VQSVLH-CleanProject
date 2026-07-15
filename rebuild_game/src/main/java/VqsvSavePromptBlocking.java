final class VqsvSavePromptBlocking implements Blocking {
    private int phase;
    private int ticks;
    private boolean saved;
    private boolean cancelled;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        s.savePromptVisible = true;
        if (phase == 0) {
            s.savePromptMessage = VqsvText.Common.SAVE_PROMPT;
            s.savePromptStatus = "";
            applyPointerChoice(s);
            if (s.keyLeft) {
                s.savePromptSelected = 1;
            } else if (s.keyRight) {
                s.savePromptSelected = 0;
            }
            if (!s.key0) {
                return false;
            }
            s.key0 = false;
            if (s.savePromptSelected == 1) {
                cancelled = true;
                close(s, "skip");
                return true;
            }
            s.savePromptStatus = VqsvText.Common.SAVE_IN_PROGRESS;
            s.sourceStateTrace.add("PORTED save opcode46 key=196640 confirm right tick"
                    + " -> f=1 game.k.k()");
            VqsvSaveRuntime.save(s);
            s.savePromptStatus = VqsvText.Common.SAVE_SUCCESS;
            saved = true;
            phase = 1;
            ticks = 0;
            return false;
        }
        ticks++;
        if (ticks >= 1 || s.key0) {
            close(s, "success-close");
            return true;
        }
        return false;
    }

    boolean savedSuccessfully() {
        return saved;
    }

    boolean cancelled() {
        return cancelled;
    }

    private void applyPointerChoice(VqsvIntroDemo.Scene s) {
        if (s.savePromptClickX < 0 || s.savePromptClickY < 0) {
            return;
        }
        VqsvUiLayout layout = VqsvUiLayout.load("msgtip.ui");
        if (withinWidget(layout, 3, s.savePromptClickX, s.savePromptClickY, 218, 298, 23, 20)) {
            s.savePromptSelected = 0;
            s.key0 = true;
            s.sourceStateTrace.add("PORTED save opcode46 pointer right tick widget=3 -> key=196640");
        } else if (withinWidget(layout, 4, s.savePromptClickX, s.savePromptClickY, 1, 298, 23, 20)) {
            s.savePromptSelected = 1;
            s.key0 = true;
            s.sourceStateTrace.add("PORTED save opcode46 pointer left X widget=4 -> key=262144");
        } else {
            s.key0 = false;
            s.sourceStateTrace.add("PORTED save opcode46 pointer ignored outside msgtip softkeys");
        }
        s.savePromptClickX = -1;
        s.savePromptClickY = -1;
    }

    private boolean withinWidget(VqsvUiLayout layout, int widgetId, int x, int y,
                                 int fallbackX, int fallbackY, int fallbackW, int fallbackH) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int wx = widget == null ? fallbackX : widget.x;
        int wy = widget == null ? fallbackY : widget.y;
        int ww = widget == null ? fallbackW : Math.max(fallbackW, widget.w);
        int wh = widget == null ? fallbackH : Math.max(fallbackH, layout.h(widgetId, fallbackH));
        return x >= wx && x < wx + ww && y >= wy && y < wy + wh;
    }

    private void close(VqsvIntroDemo.Scene s, String reason) {
        s.savePromptVisible = false;
        s.savePromptMessage = "";
        s.savePromptStatus = "";
        s.savePromptClickX = -1;
        s.savePromptClickY = -1;
        s.sourceStateTrace.add("PORTED save prompt closed reason=" + reason);
    }
}

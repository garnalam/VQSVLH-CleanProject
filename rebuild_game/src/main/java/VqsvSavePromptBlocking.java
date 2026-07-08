final class VqsvSavePromptBlocking implements Blocking {
    private int phase;
    private int ticks;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        s.savePromptVisible = true;
        if (phase == 0) {
            s.savePromptMessage = VqsvText.Common.SAVE_PROMPT;
            s.savePromptStatus = "";
            applyPointerChoice(s);
            if (s.keyLeft) {
                s.savePromptSelected = 0;
            } else if (s.keyRight) {
                s.savePromptSelected = 1;
            }
            if (!s.key0) {
                return false;
            }
            s.key0 = false;
            if (s.savePromptSelected == 1) {
                close(s, "skip");
                return true;
            }
            s.savePromptStatus = VqsvText.Common.SAVE_IN_PROGRESS;
            s.sourceStateTrace.add("PORTED/PARTIAL save opcode46 confirm yes -> game.k.k()");
            VqsvSaveRuntime.save(s);
            s.savePromptStatus = VqsvText.Common.SAVE_SUCCESS;
            phase = 1;
            ticks = 0;
            return false;
        }
        ticks++;
        if (ticks >= 24 || s.key0) {
            close(s, "success-close");
            return true;
        }
        return false;
    }

    private void applyPointerChoice(VqsvIntroDemo.Scene s) {
        if (s.savePromptClickX < 0 || s.savePromptClickY < 0) {
            return;
        }
        s.savePromptSelected = s.savePromptClickX < VqsvIntroDemo.W / 2 ? 0 : 1;
        s.savePromptClickX = -1;
        s.savePromptClickY = -1;
    }

    private void close(VqsvIntroDemo.Scene s, String reason) {
        s.savePromptVisible = false;
        s.savePromptMessage = "";
        s.savePromptStatus = "";
        s.savePromptClickX = -1;
        s.savePromptClickY = -1;
        s.sourceStateTrace.add("PORTED/PARTIAL save prompt closed reason=" + reason);
    }
}

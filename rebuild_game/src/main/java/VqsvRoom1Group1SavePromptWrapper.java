final class VqsvRoom1Group1SavePromptWrapper implements Blocking {
    private final VqsvSavePromptBlocking prompt = new VqsvSavePromptBlocking();
    private boolean entered;
    private boolean completed;

    @Override
    public boolean tick(VqsvIntroDemo.Scene s) {
        if (!entered) {
            entered = true;
            int gateState = s.sourceEventState(1, 0, 0);
            boolean gateOpen = s.op15CheckEventState(1, 0, 0);
            s.sourceStateTrace.add("PORTED/PARTIAL room1 group1 save wrapper op15 [1,0,0]"
                    + " state=" + gateState + " gateOpen=" + gateOpen
                    + " sourceAccepts=3/4");
            s.sourceStateTrace.add("PORTED/PARTIAL room1 group1 save wrapper op56 [0,1]"
                    + " actor=50 state=0 trace-only");
            s.sourceStateTrace.add("PORTED/PARTIAL room1 group1 save wrapper op46 "
                    + VqsvText.Common.SAVE_PROMPT);
        }
        boolean done = prompt.tick(s);
        if (!completed && prompt.savedSuccessfully()) {
            completeGroup(s, "op46 f=1 save-success-before-success-text");
        }
        if (!done) {
            return false;
        }
        if (!completed) {
            completeGroup(s, prompt.cancelled() ? "op46 cancel then op14" : "op14");
        }
        return true;
    }

    private void completeGroup(VqsvIntroDemo.Scene s, String reason) {
        completed = true;
        s.op14CompleteEvent(1, 1, 1);
        s.sourceStateTrace.add("PORTED/PARTIAL room1 group1 save wrapper " + reason
                + " complete [1,1,1]");
    }
}

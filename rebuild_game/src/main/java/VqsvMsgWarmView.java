final class VqsvMsgWarmView {
    static final VqsvMsgWarmView EMPTY = new VqsvMsgWarmView("", "");

    static final int FRAME_WIDGET_ID = 8;
    static final int MESSAGE_WIDGET_ID = 7;
    static final int PROMPT_WIDGET_ID = 6;
    static final int FRAME_SPRITE_CELL = 128;

    final String message;
    final String prompt;

    private VqsvMsgWarmView(String message, String prompt) {
        this.message = TextBox.decodeMojibake(message == null ? "" : message);
        this.prompt = TextBox.decodeMojibake(prompt == null ? "" : prompt);
    }

    static VqsvMsgWarmView of(String message, String prompt) {
        return new VqsvMsgWarmView(message, prompt);
    }

    boolean visible() {
        return !message.isEmpty() || !prompt.isEmpty();
    }

    String widgetText(int widgetId) {
        if (widgetId == MESSAGE_WIDGET_ID) {
            return message;
        }
        if (widgetId == PROMPT_WIDGET_ID) {
            return prompt;
        }
        return "";
    }
}

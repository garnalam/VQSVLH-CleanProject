final class VqsvOpenBoxView {
    static final VqsvOpenBoxView EMPTY = new VqsvOpenBoxView("");

    static final int FRAME_WIDGET_ID = 1;
    static final int TEXT_WIDGET_ID = 2;
    static final int SPRITE_BANK = 257;
    static final int FRAME_ANIM_STATE = 9;
    static final int READY_CURSOR = 3;

    final String message;

    private VqsvOpenBoxView(String message) {
        this.message = TextBox.decodeMojibake(message == null ? "" : message);
    }

    static VqsvOpenBoxView of(String message) {
        return new VqsvOpenBoxView(message);
    }

    boolean visible() {
        return !message.isEmpty();
    }

    String widgetText(int widgetId) {
        return widgetId == TEXT_WIDGET_ID ? message : "";
    }

    int widgetX(int widgetId, int fallback) {
        return VqsvUiLayout.load("openbox.ui").x(widgetId, fallback);
    }

    int widgetY(int widgetId, int fallback) {
        return VqsvUiLayout.load("openbox.ui").y(widgetId, fallback);
    }

    int widgetW(int widgetId, int fallback) {
        return VqsvUiLayout.load("openbox.ui").w(widgetId, fallback);
    }
}

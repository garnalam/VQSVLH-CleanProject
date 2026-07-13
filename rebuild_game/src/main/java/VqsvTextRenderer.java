import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
final class FontBitmap {
    static final java.awt.Font DISPLAY_FONT = new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 9);
    static final java.awt.font.FontRenderContext FONT_CONTEXT =
            new java.awt.font.FontRenderContext(null, false, false);
    final Map<Character, Integer> index = new HashMap<>();
    final int[] widths;
    final int[] offsets;
    final byte[][] pixels;
    final int height;
    final int spaceWord;

    FontBitmap() {
        try (DataInputStream in = new DataInputStream(VqsvIntroDemo.class.getResourceAsStream("/font.bin"))) {
            String chars = in.readUTF();
            height = in.readByte();
            widths = new int[chars.length()];
            offsets = new int[chars.length()];
            int total = 0;
            for (int i = 0; i < chars.length(); i++) {
                widths[i] = in.readByte();
                offsets[i] = total;
                total += widths[i];
                index.put(chars.charAt(i), i);
            }
            pixels = new byte[height][total];
            int bit = 7;
            int cur = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < total; x++) {
                    if (++bit >= 8) {
                        bit = 0;
                        cur = in.readByte();
                    }
                    if ((cur & 1) != 0) {
                        pixels[y][x] = 1;
                    }
                    cur >>= 1;
                }
            }
            spaceWord = width("nhung1");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    int charWidth(char c) {
        if (c == ' ') {
            return 3;
        }
        return Math.max(1, (int) Math.ceil(DISPLAY_FONT
                .getStringBounds(String.valueOf(c), FONT_CONTEXT)
                .getWidth()));
    }

    int width(String s) {
        s = TextBox.decodeMojibake(s);
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            w += charWidth(s.charAt(i));
        }
        return w;
    }

    int taggedWidth(String s) {
        s = TextBox.decodeMojibake(s);
        int w = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isHexColorTag(s, i)) {
                i += 6;
                continue;
            }
            if (isSourceColorTag(s, i)) {
                i += 1;
                continue;
            }
            w += charWidth(s.charAt(i));
        }
        return w;
    }

    void drawChar(Graphics2D g, char c, int x, int y) {
        if (c == ' ') {
            return;
        }
        java.awt.Font oldFont = g.getFont();
        Object oldAa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setFont(DISPLAY_FONT);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.drawString(String.valueOf(c), x, y + 9);
        g.setFont(oldFont);
        if (oldAa != null) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAa);
        }
    }

    char glyphChar(char c) {
        if (c == '\u0111') {
            return 'd';
        }
        if (c == '\u0110') {
            return 'D';
        }
        String decomposed = java.text.Normalizer.normalize(String.valueOf(c), java.text.Normalizer.Form.NFD);
        if (decomposed.indexOf('\u0323') >= 0 && !decomposed.isEmpty()) {
            char base = decomposed.charAt(0);
            if (index.containsKey(base)) {
                return base;
            }
        }
        if (index.containsKey(c)) {
            return c;
        }
        if (!decomposed.isEmpty()) {
            char base = decomposed.charAt(0);
            if (base != c && index.containsKey(base)) {
                return base;
            }
        }
        return c;
    }

    void drawTagged(Graphics2D g, String s, int x, int y, int maxWidth, int visibleChars) {
        s = TextBox.decodeMojibake(s);
        int cx = x;
        int cy = y;
        int color = 0xFFFFFF;
        g.setColor(new Color(color));
        int shown = 0;
        int softLimit = maxWidth - spaceWord;
        for (int i = 0; i < s.length() && shown < visibleChars; i++) {
            char ch = s.charAt(i);
            if (isHexColorTag(s, i)) {
                String hex = s.substring(i + 1, i + 7);
                color = Integer.parseInt(hex, 16);
                g.setColor(new Color(color));
                i += 6;
                continue;
            }
            if (isSourceColorTag(s, i)) {
                color = sourceTagColor(s.charAt(i + 1), color);
                g.setColor(new Color(color));
                i += 1;
                continue;
            }
            int cw = charWidth(ch);
            int nx = cx + cw;
            if (nx > x + maxWidth - 10 || ch == ' ' && nx > x + softLimit) {
                cx = x;
                cy += height + 1;
                if (ch == ' ') {
                    shown++;
                    continue;
                }
            }
            drawChar(g, ch, cx, cy);
            cx += cw;
            shown++;
        }
    }

    void drawTaggedLine(Graphics2D g, String s, int x, int y, int visibleChars, int defaultColor) {
        s = TextBox.decodeMojibake(s);
        int cx = x;
        int color = defaultColor;
        g.setColor(new Color(color));
        int shown = 0;
        for (int i = 0; i < s.length() && shown < visibleChars; i++) {
            char ch = s.charAt(i);
            if (isHexColorTag(s, i)) {
                String hex = s.substring(i + 1, i + 7);
                color = Integer.parseInt(hex, 16);
                g.setColor(new Color(color));
                i += 6;
                continue;
            }
            if (isSourceColorTag(s, i)) {
                color = sourceTagColor(s.charAt(i + 1), defaultColor);
                g.setColor(new Color(color));
                i += 1;
                continue;
            }
            drawChar(g, ch, cx, y);
            cx += charWidth(ch);
            shown++;
        }
    }

    private static boolean isHexColorTag(String s, int i) {
        if (i < 0 || i + 6 >= s.length() || s.charAt(i) != '#') {
            return false;
        }
        for (int j = i + 1; j <= i + 6; j++) {
            char ch = s.charAt(j);
            if (!(ch >= '0' && ch <= '9')
                    && !(ch >= 'a' && ch <= 'f')
                    && !(ch >= 'A' && ch <= 'F')) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSourceColorTag(String s, int i) {
        return i >= 0 && i + 1 < s.length() && s.charAt(i) == '#'
                && s.charAt(i + 1) >= '0' && s.charAt(i + 1) <= '2';
    }

    private static int sourceTagColor(char tag, int defaultColor) {
        if (tag == '2') {
            return 0xD85A00;
        }
        return defaultColor;
    }
}

final class TextBox {
    static final int W = 240;
    static final int H = 320;
    static final int SOURCE_NONE = 0;
    static final int SOURCE_OPENBOX = 1;
    static final int SOURCE_TASKTIP = 2;
    static final int SOURCE_MSGWARM = 3;
    static final int OPENBOX_FRAME_X = 45;
    static final int OPENBOX_FRAME_Y = 147;
    static final int OPENBOX_FRAME_W = 150;
    static final int OPENBOX_FRAME_H_SOURCE = -1;
    static final int OPENBOX_FRAME_ALIGN = 4;
    static final int OPENBOX_TEXT_X = 47;
    static final int OPENBOX_TEXT_Y = 154;
    static final int OPENBOX_TEXT_W = 146;
    static final int OPENBOX_TEXT_H = 26;
    static final int OPENBOX_TEXT_ALIGN = 4;
    static final int OPENBOX_TEXT_COLOR = 0x1C6C91;
    static final int TASKTIP_FRAME_X = 14;
    static final int TASKTIP_FRAME_Y = 147;
    static final int TASKTIP_FRAME_W = 212;
    static final int TASKTIP_FRAME_H_SOURCE = -1;
    static final int TASKTIP_FRAME_ALIGN = 0;
    static final int TASKTIP_TEXT_X = 16;
    static final int TASKTIP_TEXT_Y = 154;
    static final int TASKTIP_TEXT_W = 208;
    static final int TASKTIP_TEXT_H = 26;
    static final int TASKTIP_TEXT_ALIGN = 4;
    static final int TASKTIP_TEXT_COLOR = 0x1C6C91;
    static final int MSGWARM_FRAME_X = 76;
    static final int MSGWARM_FRAME_Y = 106;
    static final int MSGWARM_FRAME_W = 89;
    static final int MSGWARM_FRAME_H = 79;
    static final int MSGWARM_TEXT_X = 85;
    static final int MSGWARM_TEXT_Y = 119;
    static final int MSGWARM_TEXT_W = 70;
    static final int MSGWARM_PROMPT_X = 89;
    static final int MSGWARM_PROMPT_Y = 170;
    static final int MSGWARM_PROMPT_W = 60;
    final int x, y, w, h;
    final String text;
    final String sourcePrompt;
    final List<String> pages;
    final boolean waitKey;
    final boolean fullBackdrop;
    final boolean boxBackdrop;
    final boolean dialogBackdrop;
    final int sourceUiKind;
    final String speaker;
    final int dialogMode;
    final SpriteAnim sourceUiAnim;
    int pageIndex;
    int visibleChars;
    int doneTicks;
    int sourceTextOffset;
    boolean sourceTextInitialized;
    boolean readyForKey;
    boolean disposed;

    TextBox(int x, int y, int w, int h, String text, boolean waitKey) {
        this(x, y, w, h, text, null, waitKey, false, false, false, false, "", -1);
    }

    TextBox(int x, int y, int w, int h, String text, boolean waitKey, boolean fullBackdrop, boolean boxBackdrop) {
        this(x, y, w, h, text, null, waitKey, fullBackdrop, boxBackdrop, false, false, "", -1);
    }

    TextBox(int x, int y, int w, int h, String text, List<String> pages, boolean waitKey,
                    boolean fullBackdrop, boolean boxBackdrop, boolean dialogBackdrop, boolean openBoxBackdrop,
                    String speaker, int dialogMode) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = decodeMojibake(text);
        this.pages = normalizePages(pages);
        this.waitKey = waitKey;
        this.fullBackdrop = fullBackdrop;
        this.boxBackdrop = boxBackdrop;
        this.dialogBackdrop = dialogBackdrop;
        this.sourceUiKind = openBoxBackdrop ? SOURCE_OPENBOX : SOURCE_NONE;
        this.speaker = decodeMojibake(speaker);
        this.dialogMode = dialogMode;
        this.sourcePrompt = "";
        this.sourceUiAnim = sourceUiKind == SOURCE_NONE ? null : SpriteAnim.load(257);
        if (this.sourceUiAnim != null) {
            this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
        }
    }

    TextBox(int x, int y, int w, int h, String text, int sourceUiKind) {
        this(x, y, w, h, text, sourceUiKind, "");
    }

    TextBox(int x, int y, int w, int h, String text, int sourceUiKind, String sourcePrompt) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = decodeMojibake(text);
        this.sourcePrompt = decodeMojibake(sourcePrompt);
        this.pages = null;
        this.waitKey = true;
        this.fullBackdrop = false;
        this.boxBackdrop = false;
        this.dialogBackdrop = false;
        this.sourceUiKind = sourceUiKind;
        this.speaker = "";
        this.dialogMode = -1;
        this.sourceUiAnim = SpriteAnim.load(257);
        this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
    }

    static TextBox full(int x, int y, String text, boolean waitKey) {
        return new TextBox(x, y, W - 2 * x, H - y, text, waitKey, true, false);
    }

    static TextBox box(int x, int y, int w, int h, String text, boolean waitKey) {
        return new TextBox(x, y, w, h, text, waitKey);
    }

    static TextBox openBox(String text) {
        VqsvUiLayout layout = VqsvUiLayout.load("openbox.ui");
        return new TextBox(layout.x(2, OPENBOX_TEXT_X), layout.y(2, OPENBOX_TEXT_Y),
                layout.w(2, OPENBOX_TEXT_W), layout.h(2, OPENBOX_TEXT_H),
                text, SOURCE_OPENBOX);
    }

    static TextBox taskTip(String text) {
        return new TextBox(TASKTIP_TEXT_X, TASKTIP_TEXT_Y, TASKTIP_TEXT_W, TASKTIP_TEXT_H, text, SOURCE_TASKTIP);
    }

    static TextBox msgWarm(String text, String prompt) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
        VqsvUiLayout.UiWidget message = layout.widget(7);
        return new TextBox(layout.x(7, MSGWARM_TEXT_X), layout.y(7, MSGWARM_TEXT_Y),
                layout.w(7, MSGWARM_TEXT_W), layout.h(7, 18),
                text, SOURCE_MSGWARM, prompt);
    }

    static TextBox dialog(FontBitmap font, String speaker, String text, int mode) {
        String tagged = "#000000" + decodeMojibake(text);
        List<String> pages = paginateTagged(font, tagged, 230, 4);
        return new TextBox(6, 264, 230, 52, tagged, pages, true,
                false, false, true, false, decodeMojibake(speaker), mode);
    }

    static List<String> normalizePages(List<String> source) {
        if (source == null) {
            return null;
        }
        List<String> out = new ArrayList<>(source.size());
        for (String page : source) {
            out.add(decodeMojibake(page));
        }
        return out;
    }

    static String decodeMojibake(String text) {
        if (text == null) {
            return null;
        }
        String current = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFC);
        for (int i = 0; i < 4 && looksMojibake(current); i++) {
            String decoded = decodeMojibakeOnce(current);
            if (decoded.equals(current)) {
                break;
            }
            current = java.text.Normalizer.normalize(decoded, java.text.Normalizer.Form.NFC);
        }
        return current;
    }

    static String decodeMojibakeOnce(String text) {
        try {
            ByteBuffer bytes = java.nio.charset.Charset.forName("windows-1252")
                    .newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(java.nio.CharBuffer.wrap(text));
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(bytes)
                    .toString();
        } catch (CharacterCodingException ex) {
            return text;
        }
    }

    static boolean looksMojibake(String text) {
        return text.indexOf('\u00c3') >= 0
                || text.indexOf('\u00c2') >= 0
                || text.indexOf('\u00c4') >= 0
                || text.indexOf('\u00c5') >= 0
                || text.indexOf('\u00c6') >= 0
                || text.indexOf('\u00e2') >= 0
                || text.indexOf('\u00e1') >= 0
                || text.indexOf('\u00c1') >= 0
                || text.indexOf('\u20ac') >= 0;
    }

    void tick(FontBitmap font) {
        if (sourceUiKind != SOURCE_NONE && sourceUiAnim != null) {
            sourceUiAnim.tickHoldLast();
        }
        if (sourceUiKind != SOURCE_NONE) {
            if (!sourceUiTextReady()) {
                return;
            }
            tickSourceUiText(font);
            return;
        }
        int total = visibleLength(currentText());
        if (visibleChars < total) {
            visibleChars = Math.min(total, visibleChars + 2);
            doneTicks = 0;
        } else {
            doneTicks++;
            if (waitKey && doneTicks > 38) {
                readyForKey = true;
            }
        }
    }

    void tickSourceUiText(FontBitmap font) {
        int total = visibleLength(currentText());
        visibleChars = total;
        if (!sourceTextInitialized) {
            sourceTextOffset = 0;
            sourceTextInitialized = true;
        }
        if (sourceUiKind == SOURCE_MSGWARM) {
            VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
            int messageWidth = layout.w(7, MSGWARM_TEXT_W);
            int messageTextWidth = font.taggedWidth(currentText());
            int promptWidth = layout.w(6, MSGWARM_PROMPT_W);
            int promptTextWidth = font.taggedWidth(sourcePrompt == null ? "" : sourcePrompt);
            int overflowTextWidth = Math.max(messageTextWidth > messageWidth ? messageTextWidth : 0,
                    promptTextWidth > promptWidth ? promptTextWidth : 0);
            int overflowBoxWidth = messageTextWidth > messageWidth ? messageWidth : promptWidth;
            if (overflowTextWidth > 0 && doneTicks > 8) {
                int cycle = overflowTextWidth + overflowBoxWidth + 12;
                sourceTextOffset = (sourceTextOffset + 1) % Math.max(1, cycle);
            }
            doneTicks++;
            if (waitKey && doneTicks > 10) {
                readyForKey = true;
            }
            return;
        }
        if (font.taggedWidth(currentText()) > w && doneTicks > 8) {
            int cycle = font.taggedWidth(currentText()) + w + 12;
            sourceTextOffset = (sourceTextOffset + 1) % Math.max(1, cycle);
        }
        doneTicks++;
        if (waitKey && doneTicks > 10) {
            readyForKey = true;
        }
    }

    void render(Graphics2D g, FontBitmap font) {
        if (fullBackdrop) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, W, H);
        } else if (boxBackdrop) {
            g.setColor(Color.BLACK);
            g.fillRect(x - 4, y - 4, w + 8, h + 8);
            g.setColor(Color.WHITE);
            g.drawRect(x - 4, y - 4, w + 7, h + 7);
        } else if (dialogBackdrop) {
            renderDialogFrame(g, font);
        } else if (sourceUiKind != SOURCE_NONE) {
            renderSourceUiFrame(g);
        }
        if (sourceUiKind != SOURCE_NONE) {
            renderSourceUiText(g, font);
            return;
        }
        font.drawTagged(g, currentText(), x, y, w, visibleChars);
        if (readyForKey && (doneTicks / 5) % 2 == 0) {
            if (dialogBackdrop) {
                g.setColor(Color.BLACK);
                int[] xs = {226, 234, 230};
                int[] ys = {307, 307, 313};
                g.fillPolygon(xs, ys, 3);
            } else {
                String prompt = VqsvText.Common.PROMPT_PRESS_0;
                g.setColor(Color.WHITE);
                int px = (W - font.width(prompt)) / 2;
                font.drawTagged(g, prompt, px, H - 18, W, prompt.length());
            }
        }
    }

    void renderSourceUiFrame(Graphics2D g) {
        if (sourceUiKind == SOURCE_MSGWARM) {
            renderMsgWarmFrame(g);
            return;
        }
        if (sourceUiKind == SOURCE_OPENBOX) {
            renderOpenBoxFrame(g);
            return;
        }
        if (sourceUiAnim == null) {
            return;
        }
        if (sourceUiKind == SOURCE_TASKTIP) {
            sourceUiAnim.drawAligned(g, TASKTIP_FRAME_X, TASKTIP_FRAME_Y, TASKTIP_FRAME_W,
                    TASKTIP_FRAME_H_SOURCE, TASKTIP_FRAME_ALIGN, 0);
        }
    }

    void renderSourceUiText(Graphics2D g, FontBitmap font) {
        if (!sourceUiTextReady()) {
            return;
        }
        if (sourceUiKind == SOURCE_MSGWARM) {
            VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
            VqsvUiLayout.UiWidget message = layout.widget(7);
            VqsvUiLayout.UiWidget prompt = layout.widget(6);
            int messageX = layout.x(7, MSGWARM_TEXT_X);
            int messageY = layout.y(7, MSGWARM_TEXT_Y);
            int messageW = layout.w(7, MSGWARM_TEXT_W);
            int promptY = layout.y(6, MSGWARM_PROMPT_Y);
            drawSourceUiLine(g, font, currentText(), messageX, messageY,
                    messageW, Math.max(12, promptY - messageY - 3),
                    message == null ? OPENBOX_TEXT_ALIGN : message.b,
                    widgetTextColor(message, OPENBOX_TEXT_COLOR));
            drawSourceUiPromptLine(g, font, sourcePrompt, layout.x(6, MSGWARM_PROMPT_X),
                    promptY, layout.w(6, MSGWARM_PROMPT_W), Math.max(12, layout.h(6, 13)),
                    prompt == null ? OPENBOX_TEXT_ALIGN : prompt.b,
                    widgetTextColor(prompt, OPENBOX_TEXT_COLOR));
            return;
        }
        if (sourceUiKind == SOURCE_OPENBOX) {
            VqsvUiLayout layout = VqsvUiLayout.load("openbox.ui");
            VqsvUiLayout.UiWidget textWidget = layout.widget(2);
            int[] rect = openBoxSpriteRect();
            int textY = rect[1] + Math.max(0, (rect[3] - font.height) / 2);
            drawSourceUiLine(g, font, currentText(),
                    rect[0] + 4, textY,
                    Math.max(1, rect[2] - 8), rect[3],
                    textWidget == null ? OPENBOX_TEXT_ALIGN : textWidget.b,
                    OPENBOX_TEXT_COLOR);
            return;
        }
        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        int textWidth = font.taggedWidth(currentText());
        int align = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_ALIGN : OPENBOX_TEXT_ALIGN;
        int color = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_COLOR : OPENBOX_TEXT_COLOR;
        if (textWidth > w) {
            int drawX = x + w - sourceTextOffset;
            font.drawTaggedLine(g, currentText(), drawX, y, visibleLength(currentText()), color);
        } else {
            int drawX = align == 4 ? x + (w - textWidth) / 2 : x;
            font.drawTaggedLine(g, currentText(), drawX, y, visibleLength(currentText()), color);
        }
        g.setClip(oldClip);
    }

    void drawSourceUiLine(Graphics2D g, FontBitmap font, String value,
                          int lineX, int lineY, int lineW, int lineH, int align, int color) {
        Shape oldClip = g.getClip();
        g.clipRect(lineX, lineY, lineW, lineH);
        int textWidth = font.taggedWidth(value);
        if (textWidth > lineW) {
            int drawX = lineX - sourceTextOffset;
            font.drawTaggedLine(g, value, drawX, lineY, visibleLength(value), color);
        } else {
            int drawX = align == 4 ? lineX + (lineW - textWidth) / 2 : lineX;
            font.drawTaggedLine(g, value, drawX, lineY, visibleLength(value), color);
        }
        g.setClip(oldClip);
    }

    void renderMsgWarmFrame(Graphics2D g) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
        drawMsgWarmFill(g, layout, 1, 7, 0xc6f3ff);
        drawMsgWarmFill(g, layout, 2, 59, 0xbee6f2);
        drawMsgWarmFill(g, layout, 3, 10, 0x6cc2fb);
        drawMsgWarmFill(g, layout, 5, 54, 0x51d8e9);
        SpriteAnim ui = SpriteAnim.load(257);
        VqsvUiLayout.UiWidget frame = layout.widget(8);
        int cellId = frame != null && frame.altId >= 0 ? frame.altId : 128;
        int x = frame == null ? MSGWARM_FRAME_X : frame.x;
        int y = frame == null ? MSGWARM_FRAME_Y : frame.y;
        int[] bounds = ui.cellBounds(cellId);
        if (bounds != null) {
            ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
        }
    }

    private static void drawMsgWarmFill(Graphics2D g, VqsvUiLayout layout, int widgetId,
                                        int fallbackHeight, int fallbackRgb) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int x = layout.x(widgetId, widgetId == 5 ? 82 : 79);
        int y = layout.y(widgetId, widgetId == 1 ? 109 : widgetId == 3 ? 175 : 116);
        int w = layout.w(widgetId, widgetId == 5 ? 76 : 81);
        int h = layout.bandHeight(widgetId, fallbackHeight);
        g.setColor(new Color(widgetFillColor(widget, fallbackRgb)));
        g.fillRect(x, y, w, h);
    }

    private static int widgetFillColor(VqsvUiLayout.UiWidget widget, int fallbackRgb) {
        if (widget == null) {
            return fallbackRgb;
        }
        int source = widget.jColor;
        if ((source >>> 24) == 0 && source < 0) {
            return source & 0xffffff;
        }
        if (source == 0 || source == -16777216 || source == -1) {
            return fallbackRgb;
        }
        return source & 0xffffff;
    }

    private static int widgetTextColor(VqsvUiLayout.UiWidget widget, int fallbackRgb) {
        if (widget == null) {
            return fallbackRgb;
        }
        int source = widget.lColor;
        if ((source >>> 24) == 0 && source < 0) {
            return source & 0xffffff;
        }
        if (source == 0 || source == -16777216 || source == -1) {
            return fallbackRgb;
        }
        return source & 0xffffff;
    }

    void drawSourceUiMarqueeLine(Graphics2D g, FontBitmap font, String source,
                                 int lineX, int lineY, int lineW, int color) {
        String value = source == null ? "" : source;
        int textWidth = font.taggedWidth(value);
        int drawX = lineX;
        if (textWidth > lineW) {
            int cycle = textWidth + 12;
            drawX = lineX - (sourceTextOffset % Math.max(1, cycle));
        } else {
            drawX = lineX + Math.max(0, (lineW - textWidth) / 2);
        }
        Shape oldClip = g.getClip();
        g.clipRect(lineX, lineY, lineW, 12);
        font.drawTaggedLine(g, value, drawX, lineY, visibleLength(value), color);
        g.setClip(oldClip);
    }

    void drawSourceUiWrappedText(Graphics2D g, FontBitmap font, String source,
                                 int lineX, int lineY, int lineW, int lineH, int color) {
        String value = source == null ? "" : source;
        String tagged = String.format("#%06X%s", color & 0xffffff, value);
        Shape oldClip = g.getClip();
        g.clipRect(lineX, lineY, Math.max(1, lineW), Math.max(1, lineH));
        font.drawTagged(g, tagged, lineX, lineY, lineW, visibleLength(tagged));
        g.setClip(oldClip);
    }

    void drawSourceUiPromptLine(Graphics2D g, FontBitmap font, String source,
                                int lineX, int lineY, int lineW, int lineH,
                                int align, int color) {
        String value = source == null ? "" : source;
        int textWidth = font.taggedWidth(value);
        int drawX;
        if (textWidth > lineW) {
            int cycle = textWidth + lineW + 12;
            drawX = lineX - (sourceTextOffset % Math.max(1, cycle));
        } else {
            drawX = align == 4 ? lineX + Math.max(0, (lineW - textWidth) / 2) : lineX;
        }
        int drawY = lineY + Math.max(0, (lineH - font.height) / 2);
        Shape oldClip = g.getClip();
        g.clipRect(lineX, lineY, Math.max(1, lineW), Math.max(1, lineH));
        font.drawTaggedLine(g, value, drawX, drawY, visibleLength(value), color);
        g.setClip(oldClip);
    }

    void renderOpenBoxFrame(Graphics2D g) {
        if (sourceUiAnim != null) {
            VqsvUiLayout layout = VqsvUiLayout.load("openbox.ui");
            VqsvUiLayout.UiWidget frame = layout.widget(1);
            sourceUiAnim.drawAligned(g, layout.x(1, OPENBOX_FRAME_X), layout.y(1, OPENBOX_FRAME_Y),
                    layout.w(1, OPENBOX_FRAME_W),
                    frame == null ? OPENBOX_FRAME_H_SOURCE : frame.h,
                    frame == null ? OPENBOX_FRAME_ALIGN : frame.b, 0);
        }
    }

    int[] openBoxSpriteRect() {
        VqsvUiLayout layout = VqsvUiLayout.load("openbox.ui");
        VqsvUiLayout.UiWidget frame = layout.widget(1);
        int frameX = layout.x(1, OPENBOX_FRAME_X);
        int frameY = layout.y(1, OPENBOX_FRAME_Y);
        int frameW = layout.w(1, OPENBOX_FRAME_W);
        int frameH = frame == null ? OPENBOX_FRAME_H_SOURCE : frame.h;
        if (sourceUiAnim == null) {
            return new int[]{frameX, frameY, frameW, OPENBOX_TEXT_H};
        }
        int[] bounds = sourceUiAnim.animationBounds(9);
        if (bounds == null) {
            return new int[]{frameX, frameY, frameW, OPENBOX_TEXT_H};
        }
        int drawX = frameX + (frameW - bounds[2]) / 2 - bounds[0];
        int drawY = frameY + (frameH - bounds[3]) / 2 - bounds[1];
        return new int[]{drawX, drawY, bounds[2], bounds[3]};
    }

    boolean sourceUiTextReady() {
        if (sourceUiKind == SOURCE_MSGWARM) {
            return true;
        }
        if (sourceUiAnim == null) {
            return false;
        }
        int readyCursor = sourceUiKind == SOURCE_TASKTIP ? 4 : 3;
        return sourceUiAnim.cursor >= readyCursor;
    }

    static String visibleTaggedPrefix(String s, int visible) {
        StringBuilder out = new StringBuilder();
        int shown = 0;
        for (int i = 0; i < s.length() && shown < visible; i++) {
            char ch = s.charAt(i);
            if (ch == '#' && i + 6 < s.length()) {
                out.append(s, i, i + 7);
                i += 6;
                continue;
            }
            out.append(ch);
            shown++;
        }
        return out.toString();
    }

    boolean confirm() {
        int total = visibleLength(currentText());
        if (visibleChars < total) {
            visibleChars = total;
            doneTicks = 39;
            readyForKey = true;
            return false;
        }
        if (pages != null && pageIndex + 1 < pages.size()) {
            pageIndex++;
            visibleChars = 0;
            doneTicks = 0;
            readyForKey = false;
            return false;
        }
        disposed = true;
        return true;
    }

    String currentText() {
        if (pages == null || pages.isEmpty()) {
            return text;
        }
        return pages.get(pageIndex);
    }

    void renderDialogFrame(Graphics2D g, FontBitmap font) {
        Color border = new Color(0, 174, 205);
        g.setColor(Color.WHITE);
        g.fillRect(0, 256, 240, 64);
        g.setColor(border);
        g.drawRect(0, 256, 239, 63);
        g.drawLine(1, 257, 238, 257);

        if (dialogMode == 0 || dialogMode == 1) {
            int tabX = dialogMode == 0 ? 1 : 178;
            g.setColor(Color.WHITE);
            g.fillRect(tabX, 231, 62, 25);
            g.setColor(border);
            g.drawRect(tabX, 231, 62, 25);
            g.drawLine(tabX + 1, 255, tabX + 61, 255);
            if (speaker != null && speaker.length() > 0 && !"??".equals(speaker)) {
                font.drawTagged(g, "#000000" + speaker, tabX + 5, 239, 54, speaker.length());
            } else if ("??".equals(speaker)) {
                font.drawTagged(g, "#000000??", tabX + 22, 239, 54, 2);
            }
        }
    }

    static int visibleLength(String s) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isHexColorTag(s, i)) {
                i += 6;
            } else if (isSourceColorTag(s, i)) {
                i += 1;
            } else {
                n++;
            }
        }
        return n;
    }

    static boolean isHexColorTag(String s, int i) {
        if (i < 0 || i + 6 >= s.length() || s.charAt(i) != '#') {
            return false;
        }
        for (int j = i + 1; j <= i + 6; j++) {
            char ch = s.charAt(j);
            if (!(ch >= '0' && ch <= '9')
                    && !(ch >= 'a' && ch <= 'f')
                    && !(ch >= 'A' && ch <= 'F')) {
                return false;
            }
        }
        return true;
    }

    static boolean isSourceColorTag(String s, int i) {
        return i >= 0 && i + 1 < s.length() && s.charAt(i) == '#'
                && s.charAt(i + 1) >= '0' && s.charAt(i + 1) <= '2';
    }

    static List<String> paginateTagged(FontBitmap font, String s, int maxWidth, int maxLines) {
        List<String> out = new ArrayList<>();
        StringBuilder page = new StringBuilder();
        String color = "#FFFFFF";
        int line = 0;
        int width = 0;
        int softLimit = maxWidth - font.spaceWord;
        page.append(color);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '#' && i + 6 < s.length()) {
                color = s.substring(i, i + 7);
                page.append(color);
                i += 6;
                continue;
            }
            int cw = font.charWidth(ch);
            int next = width + cw;
            if (next > maxWidth - 10 || ch == ' ' && next > softLimit) {
                line++;
                width = 0;
                if (line >= maxLines) {
                    out.add(page.toString());
                    page = new StringBuilder();
                    page.append(color);
                    line = 0;
                }
                if (ch == ' ') {
                    continue;
                }
            }
            page.append(ch);
            width += cw;
        }
        if (visibleLength(page.toString()) > 0 || out.isEmpty()) {
            out.add(page.toString());
        }
        return out;
    }
}


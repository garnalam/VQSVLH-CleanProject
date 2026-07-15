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
    final int dialogPortraitIndex;
    final SpriteAnim sourceUiAnim;
    final SpriteAnim dialogUiAnim;
    final SpriteAnim dialogPortraitAnim;
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
        this.dialogPortraitIndex = -1;
        this.sourcePrompt = "";
        this.sourceUiAnim = sourceUiKind == SOURCE_NONE ? null : SpriteAnim.load(257);
        this.dialogUiAnim = null;
        this.dialogPortraitAnim = null;
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
        this.dialogPortraitIndex = -1;
        this.sourceUiAnim = SpriteAnim.load(257);
        this.dialogUiAnim = null;
        this.dialogPortraitAnim = null;
        this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
    }

    TextBox(int x, int y, int w, int h, String text, List<String> pages, boolean waitKey,
                    String speaker, int dialogMode, int dialogPortraitIndex) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = decodeMojibake(text);
        this.pages = normalizePages(pages);
        this.waitKey = waitKey;
        this.fullBackdrop = false;
        this.boxBackdrop = false;
        this.dialogBackdrop = true;
        this.sourceUiKind = SOURCE_NONE;
        this.speaker = decodeMojibake(speaker);
        this.dialogMode = dialogMode;
        this.dialogPortraitIndex = dialogPortraitIndex;
        this.sourcePrompt = "";
        this.sourceUiAnim = null;
        this.dialogUiAnim = SpriteAnim.load(257);
        this.dialogPortraitAnim = dialogPortraitIndex < 0 ? null : SpriteAnim.load(323);
        if (this.dialogPortraitAnim != null) {
            this.dialogPortraitAnim.setState(Math.max(0, dialogMode) + (dialogPortraitIndex << 1));
        }
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
        VqsvUiLayout layout = VqsvUiLayout.load("taskTip.ui");
        return new TextBox(layout.x(2, TASKTIP_TEXT_X), layout.y(2, TASKTIP_TEXT_Y),
                layout.w(2, TASKTIP_TEXT_W), layout.h(2, TASKTIP_TEXT_H),
                text, SOURCE_TASKTIP);
    }

    static TextBox msgWarm(String text, String prompt) {
        VqsvUiLayout layout = VqsvUiLayout.load("msgwarm.ui");
        VqsvUiLayout.UiWidget message = layout.widget(7);
        return new TextBox(layout.x(7, MSGWARM_TEXT_X), layout.y(7, MSGWARM_TEXT_Y),
                layout.w(7, MSGWARM_TEXT_W), layout.h(7, 18),
                text, SOURCE_MSGWARM, prompt);
    }

    static TextBox dialog(FontBitmap font, String speaker, String text, int mode) {
        return dialog(font, speaker, text, mode, -1);
    }

    static TextBox dialog(FontBitmap font, String speaker, String text, int mode, int portraitIndex) {
        VqsvUiLayout layout = VqsvUiLayout.load("dialog.ui");
        int textX = layout.x(14, 6);
        int textY = layout.y(14, 264);
        int textW = layout.w(14, 230);
        int textH = layout.h(14, 52);
        String tagged = "#000000" + decodeMojibake(text);
        List<String> pages = paginateTagged(font, tagged, textW, 4);
        return new TextBox(textX, textY, textW, textH, tagged, pages, true,
                decodeMojibake(speaker), mode, portraitIndex);
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
        if (dialogBackdrop && dialogPortraitAnim != null) {
            dialogPortraitAnim.tickHoldLast();
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
            VqsvUiLayout layout = VqsvUiLayout.load("taskTip.ui");
            VqsvUiLayout.UiWidget frame = layout.widget(1);
            sourceUiAnim.drawAligned(g, layout.x(1, TASKTIP_FRAME_X), layout.y(1, TASKTIP_FRAME_Y),
                    layout.w(1, TASKTIP_FRAME_W),
                    frame == null ? TASKTIP_FRAME_H_SOURCE : frame.h,
                    frame == null ? TASKTIP_FRAME_ALIGN : frame.b, 0);
            VqsvUiLayout.UiWidget prompt = layout.widget(3);
            if (prompt != null && prompt.altId >= 0) {
                int[] bounds = sourceUiAnim.cellBounds(prompt.altId);
                if (bounds != null) {
                    sourceUiAnim.drawCell(g, prompt.altId,
                            prompt.x - bounds[0], prompt.y - bounds[1], 0);
                }
            }
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
        int lastCursor = sourceUiLastCursor();
        return sourceUiAnim.cursor >= Math.min(readyCursor, lastCursor);
    }

    private int sourceUiLastCursor() {
        if (sourceUiAnim == null || sourceUiAnim.data == null
                || sourceUiAnim.data.anim == null
                || sourceUiAnim.state < 0
                || sourceUiAnim.state >= sourceUiAnim.data.anim.length) {
            return 0;
        }
        short[] frames = sourceUiAnim.data.anim[sourceUiAnim.state];
        return Math.max(0, frames.length / 2 - 1);
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
        VqsvUiLayout layout = VqsvUiLayout.load("dialog.ui");
        SpriteAnim ui = dialogUiAnim == null ? SpriteAnim.load(257) : dialogUiAnim;
        renderDialogPortrait(g, layout);
        drawDialogCell(ui, g, layout, 1, 129, 0, 256);

        if (dialogMode == 0 || dialogMode == 1) {
            int widgetId = dialogMode == 0 ? 12 : 13;
            VqsvUiLayout.UiWidget tab = layout.widget(widgetId);
            int tabX = layout.x(widgetId, dialogMode == 0 ? 1 : 178);
            int tabY = layout.y(widgetId, 231);
            int tabW = layout.w(widgetId, 62);
            drawDialogCell(ui, g, layout, widgetId, 130, tabX, tabY);
            if (speaker != null && speaker.length() > 0 && !"??".equals(speaker)) {
                int textWidth = font.taggedWidth(speaker);
                int drawX = tabX + Math.max(0, (tabW - textWidth) / 2);
                font.drawTaggedLine(g, speaker, drawX, tabY + 8, speaker.length(), 0x1c6c91);
            } else if ("??".equals(speaker)) {
                font.drawTaggedLine(g, "??", tabX + Math.max(0, (tabW - font.width("??")) / 2),
                        tabY + 8, 2, 0x1c6c91);
            }
        }
    }

    private void renderDialogPortrait(Graphics2D g, VqsvUiLayout layout) {
        if (dialogPortraitAnim == null || dialogMode < 0) {
            return;
        }
        int widgetId = dialogMode == 0 ? 11 : 8;
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int x = layout.x(widgetId, dialogMode == 0 ? -149 : 93);
        int y = layout.y(widgetId, dialogMode == 0 ? 114 : 120);
        int w = layout.w(widgetId, 311);
        int h = widget == null || widget.h <= 0 ? 142 : widget.h;
        int align = widget == null ? 8 : widget.b;
        dialogPortraitAnim.drawAligned(g, x, y, w, h, align, 0);
    }

    private static void drawDialogCell(SpriteAnim ui, Graphics2D g, VqsvUiLayout layout,
                                       int widgetId, int fallbackCell, int fallbackX, int fallbackY) {
        VqsvUiLayout.UiWidget widget = layout.widget(widgetId);
        int cellId = widget != null && widget.altId >= 0 ? widget.altId : fallbackCell;
        int[] bounds = ui.cellBounds(cellId);
        if (bounds == null) {
            return;
        }
        int x = widget == null ? fallbackX : widget.x;
        int y = widget == null ? fallbackY : widget.y;
        ui.drawCell(g, cellId, x - bounds[0], y - bounds[1], 0);
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


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
    static final int OPENBOX_FRAME_X = 45;
    static final int OPENBOX_FRAME_Y = 147;
    static final int OPENBOX_FRAME_W = 150;
    static final int OPENBOX_FRAME_H_SOURCE = -1;
    static final int OPENBOX_FRAME_ALIGN = 0;
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
    final int x, y, w, h;
    final String text;
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
        this.sourceUiAnim = sourceUiKind == SOURCE_NONE ? null : SpriteAnim.load(257);
        if (this.sourceUiAnim != null) {
            this.sourceUiAnim.setState(sourceUiKind == SOURCE_TASKTIP ? 10 : 9);
        }
    }

    TextBox(int x, int y, int w, int h, String text, int sourceUiKind) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.text = decodeMojibake(text);
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
        return new TextBox(OPENBOX_TEXT_X, OPENBOX_TEXT_Y, OPENBOX_TEXT_W, OPENBOX_TEXT_H, text, SOURCE_OPENBOX);
    }

    static TextBox taskTip(String text) {
        return new TextBox(TASKTIP_TEXT_X, TASKTIP_TEXT_Y, TASKTIP_TEXT_W, TASKTIP_TEXT_H, text, SOURCE_TASKTIP);
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
        if (sourceUiAnim == null) {
            return;
        }
        if (sourceUiKind == SOURCE_TASKTIP) {
            sourceUiAnim.drawAligned(g, TASKTIP_FRAME_X, TASKTIP_FRAME_Y, TASKTIP_FRAME_W,
                    TASKTIP_FRAME_H_SOURCE, TASKTIP_FRAME_ALIGN, 0);
        } else {
            sourceUiAnim.drawAligned(g, OPENBOX_FRAME_X, OPENBOX_FRAME_Y, OPENBOX_FRAME_W,
                    OPENBOX_FRAME_H_SOURCE, OPENBOX_FRAME_ALIGN, 0);
        }
    }

    void renderSourceUiText(Graphics2D g, FontBitmap font) {
        if (!sourceUiTextReady()) {
            return;
        }
        Shape oldClip = g.getClip();
        g.clipRect(x, y, w, h);
        int textWidth = font.taggedWidth(currentText());
        int align = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_ALIGN : OPENBOX_TEXT_ALIGN;
        int color = sourceUiKind == SOURCE_TASKTIP ? TASKTIP_TEXT_COLOR : OPENBOX_TEXT_COLOR;
        if (textWidth > w) {
            String[] lines = splitSourceUiLines(font, currentText());
            for (int i = 0; i < lines.length; i++) {
                int lineWidth = font.taggedWidth(lines[i]);
                int drawX = align == 4 ? x + Math.max(0, (w - lineWidth) / 2) : x;
                int drawY = lines.length > 1 ? y - 1 + i * font.height : y;
                font.drawTaggedLine(g, lines[i], drawX, drawY,
                        visibleLength(lines[i]), color);
            }
        } else {
            int drawX = align == 4 ? x + (w - textWidth) / 2 : x;
            font.drawTaggedLine(g, currentText(), drawX, y, visibleLength(currentText()), color);
        }
        g.setClip(oldClip);
    }

    String[] splitSourceUiLines(FontBitmap font, String source) {
        String text = source == null ? "" : source.trim();
        int totalWidth = font.taggedWidth(text);
        if (totalWidth <= w) {
            return new String[]{text};
        }
        int lineLimit = Math.max(1, w - 8);
        int best = -1;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != ' ') {
                continue;
            }
            String left = text.substring(0, i).trim();
            String right = text.substring(i + 1).trim();
            if (left.isEmpty() || right.isEmpty()) {
                continue;
            }
            int leftWidth = font.taggedWidth(left);
            int rightWidth = font.taggedWidth(right);
            if (leftWidth > lineLimit || rightWidth > lineLimit) {
                continue;
            }
            int score = Math.abs(leftWidth - rightWidth);
            if (score < bestScore) {
                bestScore = score;
                best = i;
            }
        }
        if (best >= 0) {
            return new String[]{text.substring(0, best).trim(), text.substring(best + 1).trim()};
        }
        int split = fallbackSourceUiSplit(font, text);
        return new String[]{text.substring(0, split).trim(), text.substring(split).trim()};
    }

    int fallbackSourceUiSplit(FontBitmap font, String text) {
        int split = Math.max(1, text.length() / 2);
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += font.charWidth(text.charAt(i));
            if (width >= w && i > 0) {
                split = i;
                break;
            }
        }
        return Math.max(1, Math.min(text.length() - 1, split));
    }

    boolean sourceUiTextReady() {
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


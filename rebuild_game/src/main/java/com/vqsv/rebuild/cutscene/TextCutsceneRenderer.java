package com.vqsv.rebuild.cutscene;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.render.BitmapFont;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public final class TextCutsceneRenderer {
    private static final int MODE_0 = 0;
    private static final int DEFAULT_COLOR = 0xFFFFFF;
    private static final int FINISH_DELAY_MS = 2500;
    private static final String PROMPT = "Nhấn nút 0 để tiếp tục";

    private final BitmapFont font;
    private int mode;
    private char[] text;
    private int x;
    private int y;
    private int wrapWidth;
    private int lineCountOrPageHeight;
    private int layout;
    private int visibleEnd;
    private int typeCursor;
    private int lineWidth;
    private int blinkCounter;
    private boolean active;
    private boolean complete;
    private boolean waitForConfirm;
    private long finishDeadline;

    public TextCutsceneRenderer(BitmapFont font) {
        this.font = font;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.wrapWidth = GameConfig.LOGICAL_WIDTH - 2 * x;
    }

    public void setMode0(String text, int layout) {
        this.mode = MODE_0;
        this.text = text.toCharArray();
        this.layout = layout;
        this.lineCountOrPageHeight = estimateLineCount();
        this.visibleEnd = 0;
        this.typeCursor = 0;
        this.lineWidth = 0;
        this.blinkCounter = 0;
        this.finishDeadline = 0L;
        this.active = true;
        this.complete = false;
        this.waitForConfirm = false;
    }

    public void setWaitForConfirm(boolean waitForConfirm) {
        this.waitForConfirm = waitForConfirm;
    }

    public void setBox(int width, int height) {
        this.lineCountOrPageHeight = height / Math.max(1, font.height());
        this.wrapWidth = width;
    }

    public boolean active() {
        return active;
    }

    public boolean complete() {
        return complete;
    }

    public boolean canConfirm() {
        return active && waitForConfirm && complete;
    }

    public int visibleEnd() {
        return Math.min(visibleEnd, text == null ? 0 : text.length);
    }

    public int missingGlyphCount() {
        if (text == null) {
            return 0;
        }
        int missing = 0;
        for (int index = 0; index < text.length; index++) {
            if (isColorTagStart(index)) {
                index += 6;
                continue;
            }
            char value = text[index];
            if (value != ' ' && !font.hasGlyph(value)) {
                missing++;
            }
        }
        return missing;
    }

    public void acknowledge() {
        if (!canConfirm()) {
            return;
        }
        clear();
    }

    public void clear() {
        text = null;
        active = false;
        complete = false;
        waitForConfirm = false;
        blinkCounter = 0;
    }

    public void tick(long nowMillis) {
        if (!active || mode != MODE_0 || text == null) {
            return;
        }
        for (int loop = 0; loop < 2; loop++) {
            if (visibleEnd < text.length) {
                finishDeadline = 0L;
                advanceVisibleEnd();
            } else if (finishDeadline == 0L) {
                finishDeadline = nowMillis + FINISH_DELAY_MS;
            } else if (nowMillis > finishDeadline) {
                if (!waitForConfirm) {
                    clear();
                    return;
                }
                complete = true;
                blinkCounter++;
            }
        }
    }

    public void render(Graphics2D graphics) {
        if (!active || mode != MODE_0 || text == null) {
            return;
        }
        int drawY = layout == 1 ? y - (font.height() >> 1) : y;
        graphics.setColor(new Color(DEFAULT_COLOR));
        drawMode0(graphics, x, drawY);
        if (waitForConfirm && complete && blinkCounter % 10 < 5) {
            drawPrompt(graphics);
        }
    }

    private void advanceVisibleEnd() {
        if (typeCursor == 0) {
            lineWidth = 0;
        }
        if (typeCursor < text.length && text[typeCursor] == '#') {
            visibleEnd += 7;
            return;
        }
        if (typeCursor < text.length) {
            int rightLimit = wrapWidth - 10;
            int wordReserve = rightLimit - font.wrapReserveWidth();
            char value = text[typeCursor];
            int nextWidth = lineWidth + font.glyphWidth(value);
            if (nextWidth > rightLimit || value == ' ' && nextWidth > wordReserve) {
                lineWidth = value == ' ' ? 0 : font.glyphWidth(value);
            } else {
                lineWidth = nextWidth;
            }
            typeCursor++;
            visibleEnd++;
        } else {
            visibleEnd = text.length;
        }
        if (visibleEnd > text.length) {
            visibleEnd = text.length;
        }
    }

    private void drawMode0(Graphics2D graphics, int startX, int startY) {
        int cursorX = startX;
        int cursorY = startY;
        int rightLimit = startX + wrapWidth - 10;
        int wordReserve = rightLimit - font.wrapReserveWidth();
        int color = DEFAULT_COLOR;

        for (int index = 0; index < visibleEnd && index < text.length; index++) {
            if (isColorTagStart(index)) {
                color = parseColorTag(index);
                graphics.setColor(new Color(color));
                index += 6;
                continue;
            }
            char value = text[index];
            int glyphWidth = font.glyphWidth(value);
            int nextX = cursorX + glyphWidth;
            if (nextX > rightLimit || value == ' ' && nextX > wordReserve) {
                cursorX = startX;
                nextX = startX;
                if (value != ' ') {
                    nextX = cursorX + glyphWidth;
                }
                cursorY += font.height() + 1;
            }
            graphics.setColor(new Color(color));
            font.drawChar(graphics, value, cursorX, cursorY);
            cursorX = nextX;
        }
    }

    private void drawPrompt(Graphics2D graphics) {
        Font oldFont = graphics.getFont();
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        graphics.setColor(Color.WHITE);
        FontMetrics metrics = graphics.getFontMetrics();
        int promptX = (GameConfig.LOGICAL_WIDTH - metrics.stringWidth(PROMPT)) / 2;
        int promptY = GameConfig.LOGICAL_HEIGHT - 8;
        graphics.drawString(PROMPT, promptX, promptY);
        graphics.setFont(oldFont);
    }

    private int estimateLineCount() {
        if (text == null) {
            return 0;
        }
        int lineCount = 0;
        int currentWidth = 0;
        int rightLimit = wrapWidth - 10;
        int wordReserve = rightLimit - font.wrapReserveWidth();
        for (int index = 0; index < text.length; index++) {
            if (isColorTagStart(index)) {
                index += 6;
                continue;
            }
            char value = text[index];
            int glyphWidth = font.glyphWidth(value);
            int nextWidth = currentWidth + glyphWidth;
            if (nextWidth > rightLimit || value == ' ' && nextWidth > wordReserve) {
                lineCount++;
                currentWidth = value == ' ' ? 0 : glyphWidth;
            } else {
                currentWidth = nextWidth;
            }
        }
        if (currentWidth > 0) {
            lineCount++;
        }
        return lineCount;
    }

    private boolean isColorTagStart(int index) {
        return text != null && index + 6 < text.length && text[index] == '#';
    }

    private int parseColorTag(int index) {
        int color = 0;
        for (int offset = 1; offset <= 6; offset++) {
            color = (color << 4) | hexValue(text[index + offset]);
        }
        return color;
    }

    private static int hexValue(char value) {
        if (value >= '0' && value <= '9') {
            return value - '0';
        }
        if (value >= 'a' && value <= 'f') {
            return value - 'a' + 10;
        }
        if (value >= 'A' && value <= 'F') {
            return value - 'A' + 10;
        }
        return 0;
    }
}

package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ResourceException;
import com.vqsv.rebuild.resource.ResourceLocator;

import java.awt.Graphics2D;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class BitmapFont {
    public static final int HCENTER = 1;
    public static final int VCENTER = 2;
    public static final int RIGHT = 8;
    public static final int BOTTOM = 0x20;

    private final String glyphs;
    private final int height;
    private final int[] widths;
    private final int[] offsets;
    private final byte[][] pixels;
    private final Map<Character, Integer> glyphIndex;
    private final int totalBitmapWidth;
    private final int wrapReserveWidth;

    private BitmapFont(String glyphs, int height, int[] widths, int[] offsets, byte[][] pixels,
                       Map<Character, Integer> glyphIndex, int totalBitmapWidth) {
        this.glyphs = glyphs;
        this.height = height;
        this.widths = widths;
        this.offsets = offsets;
        this.pixels = pixels;
        this.glyphIndex = glyphIndex;
        this.totalBitmapWidth = totalBitmapWidth;
        this.wrapReserveWidth = measureTextOriginalApi("nhung1");
    }

    public static BitmapFont load(AssetPaths paths) {
        byte[] data = new ResourceLocator(paths).readBytes(paths.fontBin());
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(data))) {
            String glyphs = input.readUTF();
            int height = input.readByte();
            int glyphCount = glyphs.length();
            int[] widths = new int[glyphCount];
            int[] offsets = new int[glyphCount];
            Map<Character, Integer> glyphIndex = new HashMap<>();

            int totalWidth = 0;
            for (int index = 0; index < glyphCount; index++) {
                widths[index] = input.readByte();
                offsets[index] = totalWidth;
                totalWidth += widths[index];
                glyphIndex.put(glyphs.charAt(index), index);
            }

            byte[][] pixels = new byte[height][totalWidth];
            int bitIndex = 7;
            byte packedByte = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < totalWidth; x++) {
                    if (++bitIndex >= 8) {
                        bitIndex = 0;
                        packedByte = input.readByte();
                    }
                    if ((packedByte & 1) != 0) {
                        pixels[y][x] = 1;
                    }
                    packedByte = (byte) (packedByte >> 1);
                }
            }
            return new BitmapFont(glyphs, height, widths, offsets, pixels, glyphIndex, totalWidth);
        } catch (IOException exception) {
            throw new ResourceException("Cannot parse font.bin", exception);
        }
    }

    public int height() {
        return height;
    }

    public int glyphCount() {
        return glyphs.length();
    }

    public int totalBitmapWidth() {
        return totalBitmapWidth;
    }

    public int wrapReserveWidth() {
        return wrapReserveWidth;
    }

    public boolean hasGlyph(char value) {
        return glyphIndex.containsKey(value);
    }

    public int glyphWidth(char value) {
        Integer index = glyphIndex.get(value);
        if (index == null) {
            return 0;
        }
        return widths[index];
    }

    public int measureTextOriginalApi(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return measureRange(text, 0, text.length() - 1);
    }

    public int measureDrawnText(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return measureRange(text, 0, text.length());
    }

    public int measureRange(String text, int startInclusive, int endExclusive) {
        int width = 0;
        int start = Math.max(0, startInclusive);
        int end = Math.min(text.length(), Math.max(start, endExclusive));
        for (int index = start; index < end; index++) {
            width += glyphWidth(text.charAt(index));
        }
        return width;
    }

    public int drawChar(Graphics2D graphics, char value, int x, int y) {
        Integer index = glyphIndex.get(value);
        if (index == null) {
            return 0;
        }
        int width = widths[index];
        int offset = offsets[index];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (pixels[row][offset + col] != 0) {
                    graphics.drawLine(x + col, y + row, x + col, y + row);
                }
            }
        }
        return width;
    }

    public void drawString(Graphics2D graphics, String text, int x, int y) {
        int cursor = x;
        for (int index = 0; index < text.length(); index++) {
            cursor += drawChar(graphics, text.charAt(index), cursor, y);
        }
    }

    public void drawString(Graphics2D graphics, String text, int x, int y, int anchor) {
        int drawX = x;
        int drawY = y;
        if ((anchor & HCENTER) != 0) {
            drawX = x - measureTextOriginalApi(text) / 2;
        } else if ((anchor & RIGHT) != 0) {
            drawX = x - measureTextOriginalApi(text);
        }
        if ((anchor & VCENTER) != 0) {
            drawY = y - height / 2;
        } else if ((anchor & BOTTOM) != 0) {
            drawY = y - height;
        }
        drawString(graphics, text, drawX, drawY);
    }

    public String[] wrap(String text, int maxWidth) {
        String[] lines = new String[50];
        int textLength = text.length();
        int wordReserve = maxWidth - wrapReserveWidth;
        int lineWidth = 0;
        int lineCount = 0;
        StringBuilder builder = new StringBuilder();

        for (int index = 0; index < textLength; index++) {
            char value = text.charAt(index);
            int charWidth = glyphWidth(value);
            lineWidth += charWidth;
            if (lineWidth > maxWidth || value == ' ' && lineWidth > wordReserve) {
                lines[lineCount++] = builder.toString();
                builder = new StringBuilder();
                if (value != ' ') {
                    lineWidth = charWidth;
                    builder.append(value);
                } else {
                    lineWidth = 0;
                }
            } else {
                builder.append(value);
            }
        }
        if (builder.length() > 0) {
            lines[lineCount++] = builder.toString();
        }

        String[] result = new String[lineCount];
        System.arraycopy(lines, 0, result, 0, lineCount);
        return result;
    }
}

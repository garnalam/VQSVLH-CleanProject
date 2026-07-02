package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ResourceException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class BitmapFontSmokeCheck {
    private BitmapFontSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        BitmapFont font = BitmapFont.load(paths);
        List<String> lines = new ArrayList<>();
        lines.add("bitmapFont=height:" + font.height()
                + " glyphs:" + font.glyphCount()
                + " totalBitmapWidth:" + font.totalBitmapWidth()
                + " wrapReserveWidth:" + font.wrapReserveWidth());
        lines.add("bitmapFontWidths=n:" + font.glyphWidth('n')
                + " h:" + font.glyphWidth('h')
                + " space:" + font.glyphWidth(' ')
                + " zero:" + font.glyphWidth('0'));
        lines.add("bitmapFontMeasure=originalApi(nhung1):" + font.measureTextOriginalApi("nhung1")
                + " drawn(nhung1):" + font.measureDrawnText("nhung1"));
        String introProbe = "Nghe \u0111\u1ed3n Thi\u00ean \u0110\u1ecba chi s\u01a1 v\u1ea1n n\u0103m tr\u01b0\u1edbc";
        lines.add("bitmapFontIntroProbe=missingGlyphs:" + countMissingGlyphs(font, introProbe)
                + " drawnWidth:" + font.measureDrawnText(introProbe));

        String[] wrapped = font.wrap("Nghe don Thien Dia chi so", 80);
        lines.add("bitmapFontWrap=lines:" + wrapped.length + " first:" + (wrapped.length > 0 ? wrapped[0] : ""));

        BufferedImage image = new BufferedImage(120, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            font.drawString(graphics, "VQSV 0", 0, 0);
        } finally {
            graphics.dispose();
        }
        int litPixels = countNonTransparentPixels(image);
        if (litPixels <= 0) {
            throw new ResourceException("Bitmap font smoke draw produced no pixels");
        }
        lines.add("bitmapFontDraw=samplePixels:" + litPixels);
        return lines;
    }

    private static int countMissingGlyphs(BitmapFont font, String text) {
        int missing = 0;
        for (int index = 0; index < text.length(); index++) {
            if (!font.hasGlyph(text.charAt(index))) {
                missing++;
            }
        }
        return missing;
    }

    private static int countNonTransparentPixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (((image.getRGB(x, y) >>> 24) & 0xFF) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}

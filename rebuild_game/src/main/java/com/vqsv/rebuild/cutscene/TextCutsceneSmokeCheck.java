package com.vqsv.rebuild.cutscene;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.render.BitmapFont;
import com.vqsv.rebuild.resource.AssetPaths;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class TextCutsceneSmokeCheck {
    private static final String OPENING_TEXT = "#FFFFFF Nghe đồn Thiên Địa chi sơ, vạn năm về trước có hai vị thần, "
            + "một người duy trì trật tự, một người cai quản thế giới hỗn loạn, kiềm chế lẫn nhau, duy trì cân bằng của thế giới.";

    private TextCutsceneSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        List<String> lines = new ArrayList<>();
        BitmapFont font = BitmapFont.load(paths);
        TextCutsceneRenderer renderer = new TextCutsceneRenderer(font);
        renderer.setPosition(30, 90);
        renderer.setMode0(OPENING_TEXT, 0);
        renderer.setWaitForConfirm(true);

        int ticks = 0;
        long now = 0L;
        while (renderer.visibleEnd() < OPENING_TEXT.length() && ticks < 120) {
            renderer.tick(now);
            now += 33L;
            ticks++;
        }
        renderer.tick(now);
        renderer.tick(now + 2501L);

        BufferedImage image = new BufferedImage(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            renderer.render(graphics);
        } finally {
            graphics.dispose();
        }

        lines.add("textCutsceneMode0=visibleEnd:" + renderer.visibleEnd()
                + " complete:" + renderer.complete()
                + " canConfirm:" + renderer.canConfirm()
                + " ticksToReveal:" + ticks
                + " missingGlyphs:" + renderer.missingGlyphCount()
                + " samplePixels:" + countNonTransparentPixels(image));
        return lines;
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

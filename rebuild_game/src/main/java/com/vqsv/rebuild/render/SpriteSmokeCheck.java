package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class SpriteSmokeCheck {
    private SpriteSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        List<String> lines = new ArrayList<>();
        SpriteTable table = SpriteTable.load(paths);
        SpriteAnimator animator = SpriteAnimator.load(paths, 0, false);
        SpriteMetadata metadata = animator.metadata();
        SpriteMetadata specialMetadata = SpriteMetadata.load(paths, 86);
        lines.add("spriteTable=rows:" + table.size()
                + " row0Spr:" + table.sprId(0)
                + " row0Images:" + table.imageIds(0).length);
        lines.add("sprite0=frames:" + metadata.frameCount()
                + " cells:" + metadata.cellCount()
                + " anims:" + metadata.animationCount()
                + " currentCell:" + animator.currentCellId());
        lines.add("sprite86Special=frames:" + specialMetadata.frameCount()
                + " cells:" + specialMetadata.cellCount()
                + " anims:" + specialMetadata.animationCount());

        BufferedImage canvas = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            animator.draw(graphics, 48, 48, (byte) 0);
        } finally {
            graphics.dispose();
        }
        lines.add("sprite0Draw=samplePixels:" + countNonTransparentPixels(canvas));
        lines.add("sprite0Dirs=pixels0:" + drawDirectionPixels(animator, (byte) 0)
                + " pixels1:" + drawDirectionPixels(animator, (byte) 1)
                + " pixels3:" + drawDirectionPixels(animator, (byte) 3)
                + " pixels4:" + drawDirectionPixels(animator, (byte) 4));
        int before = animator.currentCellId();
        for (int i = 0; i < 4; i++) {
            animator.tick();
        }
        lines.add("sprite0Tick=beforeCell:" + before + " afterCell:" + animator.currentCellId());
        int animatedRow = firstMultiCellAnimation(metadata);
        if (animatedRow >= 0) {
            animator.setAnimation((byte) animatedRow, (byte) -1, true);
            int animBefore = animator.currentCellId();
            for (int i = 0; i < firstKeyDuration(metadata, animatedRow) + 1; i++) {
                animator.tick();
            }
            lines.add("sprite0AnimTick=row:" + animatedRow + " beforeCell:" + animBefore + " afterCell:" + animator.currentCellId());
        }
        return lines;
    }

    private static int firstMultiCellAnimation(SpriteMetadata metadata) {
        short[][] rows = metadata.animations();
        for (int row = 0; row < rows.length; row++) {
            if (rows[row].length < 4) {
                continue;
            }
            int firstCell = rows[row][1];
            for (int index = 3; index < rows[row].length; index += 2) {
                if (rows[row][index] != firstCell) {
                    return row;
                }
            }
        }
        return -1;
    }

    private static int firstKeyDuration(SpriteMetadata metadata, int row) {
        return metadata.animations()[row][0];
    }

    private static int drawDirectionPixels(SpriteAnimator animator, byte direction) {
        BufferedImage canvas = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            animator.draw(graphics, 48, 48, direction);
        } finally {
            graphics.dispose();
        }
        return countNonTransparentPixels(canvas);
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

package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.ImageLoader;
import com.vqsv.rebuild.resource.ResourceException;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class SpriteAnimator {
    private static final int[] DIR_0 = new int[]{0, 5, 3, 6, 2, 4, 1, 7};
    private static final int[] DIR_1 = new int[]{2, 4, 1, 7, 0, 5, 3, 6};
    private static final int[] DIR_3 = new int[]{3, 6, 0, 5, 1, 7, 2, 4};
    private static final int[] DIR_4 = new int[]{1, 7, 2, 4, 3, 6, 0, 5};

    private final SpriteMetadata metadata;
    private final BufferedImage[] images;
    private byte animation;
    private byte endBehavior = -1;
    private int keyframe;
    private int durationCounter;
    private int keyframeCount;

    private SpriteAnimator(SpriteMetadata metadata, BufferedImage[] images) {
        this.metadata = metadata;
        this.images = images;
        setAnimation((byte) 0, (byte) -1, true);
    }

    public static SpriteAnimator load(AssetPaths paths, int spriteIndex, boolean extendedAnimation) {
        SpriteTable table = SpriteTable.load(paths);
        int sprId = table.sprId(spriteIndex);
        if (sprId < 0) {
            throw new ResourceException("Sprite index has no spr id: " + spriteIndex);
        }
        SpriteMetadata metadata = SpriteMetadata.load(paths, sprId);
        metadata.setExtendedAnimation(extendedAnimation);

        ImageLoader imageLoader = new ImageLoader(paths);
        int[] imageIds = table.imageIds(spriteIndex);
        BufferedImage[] images = new BufferedImage[imageIds.length];
        for (int index = 0; index < imageIds.length; index++) {
            if (imageIds[index] < 0) {
                throw new ResourceException("Sprite index " + spriteIndex + " has invalid image id at slot " + index);
            }
            images[index] = imageLoader.loadDecodedImage(imageIds[index]);
        }
        return new SpriteAnimator(metadata, images);
    }

    public SpriteMetadata metadata() {
        return metadata;
    }

    public int currentCellId() {
        short[] row = metadata.animations()[animation];
        return metadata.extendedAnimation() ? row[(keyframe << 2) + 1] : row[(keyframe << 1) + 1];
    }

    public void setAnimation(byte animation, byte endBehavior, boolean force) {
        if (this.animation != animation || force) {
            this.animation = animation;
            this.keyframe = 0;
        } else {
            this.animation = animation;
        }
        this.endBehavior = endBehavior;
        loadKeyframe(this.keyframe);
    }

    public boolean tick() {
        if (durationCounter > 0) {
            durationCounter--;
            return false;
        }
        keyframe++;
        if (keyframe >= keyframeCount) {
            if (endBehavior >= 0) {
                setAnimation(endBehavior, (byte) -1, true);
            } else if (endBehavior == -2) {
                keyframe--;
                loadKeyframe(keyframe);
            } else if (endBehavior == -1) {
                loadKeyframe(0);
            }
            return true;
        }
        loadKeyframe(keyframe);
        return false;
    }

    public void draw(Graphics2D graphics, int x, int y, byte direction) {
        drawCell(graphics, currentCellId(), x, y, direction);
    }

    public void drawCell(Graphics2D graphics, int cellId, int x, int y, byte direction) {
        short[] cell = metadata.cells()[cellId];
        if (cell.length <= 0) {
            return;
        }
        for (int part = 0; part < cell.length; part += 4) {
            int frameId = cell[part];
            int offsetX = cell[part + 1];
            int offsetY = cell[part + 2];
            int transformId = cell[part + 3];
            drawPart(graphics, frameId, x, y, offsetX, offsetY, transformId, direction);
        }
    }

    private void drawPart(Graphics2D graphics, int frameId, int x, int y, int offsetX, int offsetY,
                          int transformId, byte direction) {
        short[] frames = metadata.frames();
        int frameBase = frameId * 5;
        int imageSlot = frames[frameBase];
        int sx = frames[frameBase + 1];
        int sy = frames[frameBase + 2];
        int width = frames[frameBase + 3];
        int height = frames[frameBase + 4];
        int drawX = x + offsetX;
        int drawY = y + offsetY;
        int transform = mapTransform(transformId, direction);

        if (direction == 1) {
            drawX = transformId % 2 == 1 ? x - offsetX - height : x - offsetX - width;
        } else if (direction == 3) {
            drawX = transformId % 2 == 1 ? x - offsetX - height : x - offsetX - width;
            drawY = transformId % 2 == 1 ? y - offsetY - width : y - offsetY - height;
        } else if (direction == 4) {
            drawY = transformId % 2 == 1 ? y - offsetY - width : y - offsetY - height;
        }

        MidpTransform.drawRegion(graphics, images[imageSlot], sx, sy, width, height, transform, drawX, drawY);
    }

    private void loadKeyframe(int keyframe) {
        this.keyframe = keyframe;
        short[] row = metadata.animations()[animation];
        if (metadata.extendedAnimation()) {
            durationCounter = row[keyframe << 2];
            keyframeCount = row.length / 4;
        } else {
            durationCounter = row[keyframe << 1];
            keyframeCount = row.length / 2;
        }
        if (durationCounter > 0) {
            durationCounter--;
        }
    }

    private static int mapTransform(int transformId, byte direction) {
        switch (direction) {
            case 1:
                return DIR_1[transformId];
            case 3:
                return DIR_3[transformId];
            case 4:
                return DIR_4[transformId];
            case 0:
            default:
                return DIR_0[transformId];
        }
    }
}

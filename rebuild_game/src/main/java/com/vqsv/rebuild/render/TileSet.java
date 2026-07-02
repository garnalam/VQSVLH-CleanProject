package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryReader;
import com.vqsv.rebuild.resource.ImageLoader;
import com.vqsv.rebuild.resource.ResourceLocator;

import java.awt.image.BufferedImage;

public final class TileSet {
    private final int modId;
    private final BufferedImage[] images;
    private final short[][] rects;

    private TileSet(int modId, BufferedImage[] images, short[][] rects) {
        this.modId = modId;
        this.images = images;
        this.rects = rects;
    }

    public static TileSet load(AssetPaths paths, MapModInfo modInfo, int modId) {
        ImageLoader imageLoader = new ImageLoader(paths);
        short[] imageIds = modInfo.imageIds(modId);
        BufferedImage[] images = new BufferedImage[imageIds.length];
        for (int index = 0; index < imageIds.length; index++) {
            images[index] = imageLoader.loadDecodedImage(imageIds[index]);
        }

        BinaryReader reader = new ResourceLocator(paths).binary(paths.modOriginal(modId));
        int rectCount = reader.readShort();
        short[][] rects = new short[rectCount][5];
        for (int index = 0; index < rectCount; index++) {
            rects[index][0] = reader.readByte();
            rects[index][1] = reader.readShort();
            rects[index][2] = reader.readShort();
            rects[index][3] = reader.readShort();
            rects[index][4] = reader.readShort();
        }
        return new TileSet(modId, images, rects);
    }

    public int modId() {
        return modId;
    }

    public int imageCount() {
        return images.length;
    }

    public int rectCount() {
        return rects.length;
    }

    public BufferedImage imageForRect(int rectId) {
        return images[rects[rectId][0]];
    }

    public short[] rect(int rectId) {
        return rects[rectId];
    }
}

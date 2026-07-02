package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryReader;
import com.vqsv.rebuild.resource.ResourceException;
import com.vqsv.rebuild.resource.ResourceLocator;

public final class MapModInfo {
    private final short[][] imageIdsByMod;

    private MapModInfo(short[][] imageIdsByMod) {
        this.imageIdsByMod = imageIdsByMod;
    }

    public static MapModInfo load(AssetPaths paths) {
        BinaryReader reader = new ResourceLocator(paths).binary(paths.modInfoOriginal());
        int modCount = reader.readByte();
        if (modCount < 0) {
            throw new ResourceException("Negative mod count in modInfo.mid: " + modCount);
        }
        short[][] rows = new short[modCount][];
        for (int mod = 0; mod < modCount; mod++) {
            int imageCount = reader.readByte();
            if (imageCount < 0) {
                throw new ResourceException("Negative image count in modInfo row: " + mod);
            }
            rows[mod] = new short[imageCount];
            for (int index = 0; index < imageCount; index++) {
                rows[mod][index] = reader.readShort();
            }
        }
        return new MapModInfo(rows);
    }

    public int modCount() {
        return imageIdsByMod.length;
    }

    public short[] imageIds(int modId) {
        if (modId < 0 || modId >= imageIdsByMod.length) {
            throw new ResourceException("Map mod id out of range: " + modId);
        }
        return imageIdsByMod[modId];
    }
}

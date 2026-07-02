package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryTables;
import com.vqsv.rebuild.resource.ResourceLocator;

public final class SpriteTable {
    private final short[][] rows;

    private SpriteTable(short[][] rows) {
        this.rows = rows;
    }

    public static SpriteTable load(AssetPaths paths) {
        ResourceLocator locator = new ResourceLocator(paths);
        return new SpriteTable(BinaryTables.readShortRows(locator.binary(paths.spriteTableOriginal())));
    }

    public int size() {
        return rows.length;
    }

    public short[] row(int spriteIndex) {
        if (spriteIndex < 0 || spriteIndex >= rows.length) {
            throw new IllegalArgumentException("Sprite index out of range: " + spriteIndex);
        }
        return rows[spriteIndex];
    }

    public int sprId(int spriteIndex) {
        short[] row = row(spriteIndex);
        if (row.length == 0) {
            return -1;
        }
        return row[0];
    }

    public int[] imageIds(int spriteIndex) {
        short[] row = row(spriteIndex);
        int[] ids = new int[Math.max(0, row.length - 1)];
        for (int index = 1; index < row.length; index++) {
            ids[index - 1] = row[index];
        }
        return ids;
    }
}

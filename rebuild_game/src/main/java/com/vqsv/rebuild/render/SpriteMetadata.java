package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryReader;
import com.vqsv.rebuild.resource.BinaryTables;
import com.vqsv.rebuild.resource.ResourceLocator;

public final class SpriteMetadata {
    private final int sprId;
    private final short[] frames;
    private final short[][] cells;
    private final short[][] animations;
    private final short[][] hitBoxes;
    private final short[][] collisionBoxes;
    private boolean extendedAnimation;

    private SpriteMetadata(int sprId, short[] frames, short[][] cells, short[][] animations,
                           short[][] hitBoxes, short[][] collisionBoxes) {
        this.sprId = sprId;
        this.frames = frames;
        this.cells = cells;
        this.animations = animations;
        this.hitBoxes = hitBoxes;
        this.collisionBoxes = collisionBoxes;
    }

    public static SpriteMetadata load(AssetPaths paths, int sprId) {
        ResourceLocator locator = new ResourceLocator(paths);
        BinaryReader reader = locator.binary(paths.sprOriginal(sprId));
        short[] frames = BinaryTables.readPackedFlatShorts(reader);
        short[][] cells = BinaryTables.readPackedShortRows(reader);
        short[][] animations = BinaryTables.readPackedShortRows(reader);
        short[][] hitBoxes = remapBoxes(BinaryTables.readPackedFlatShorts(reader), cells.length);
        short[][] collisionBoxes = remapBoxes(BinaryTables.readPackedFlatShorts(reader), cells.length);
        return new SpriteMetadata(sprId, frames, cells, animations, hitBoxes, collisionBoxes);
    }

    public int sprId() {
        return sprId;
    }

    public int frameCount() {
        return frames == null ? 0 : frames.length / 5;
    }

    public int cellCount() {
        return cells == null ? 0 : cells.length;
    }

    public int animationCount() {
        return animations == null ? 0 : animations.length;
    }

    public short[] frames() {
        return frames;
    }

    public short[][] cells() {
        return cells;
    }

    public short[][] animations() {
        return animations;
    }

    public short[][] hitBoxes() {
        return hitBoxes;
    }

    public short[][] collisionBoxes() {
        return collisionBoxes;
    }

    public boolean extendedAnimation() {
        return extendedAnimation;
    }

    public void setExtendedAnimation(boolean extendedAnimation) {
        this.extendedAnimation = extendedAnimation;
    }

    private static short[][] remapBoxes(short[] flatBoxes, int cellCount) {
        if (flatBoxes == null) {
            return null;
        }
        short[][] mapped = new short[cellCount][];
        for (int index = 0; index < flatBoxes.length / 5; index++) {
            int base = index * 5;
            int cellId = flatBoxes[base];
            short[] box = new short[]{flatBoxes[base + 1], flatBoxes[base + 2], flatBoxes[base + 3], flatBoxes[base + 4]};
            mapped[cellId] = BinaryTables.append(mapped[cellId], box);
        }
        return mapped;
    }
}

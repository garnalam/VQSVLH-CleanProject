import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.render.SpriteTable;
import com.vqsv.rebuild.resource.AssetPaths;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
final class SpriteAnim {
    static final int[][] SPRITE_TO_IMGS;
    static final SpriteTable SOURCE_SPRITE_TABLE = loadSourceSpriteTable();
    static final Map<String, SpriteData> CACHE = new HashMap<>();
    final SpriteData data;
    int state;
    int cursor;
    int delay;

    static {
        SPRITE_TO_IMGS = new int[400][];
        int[][] rows = {
                {0, 0, 100}, {8, 8, 108}, {30, 30, 126},
                {13, 13, 113}, {17, 17, 117}, {29, 29, 126}, {31, 31, 126}, {32, 32, 126},
                {7, 7, 108}, {81, 81, 159}, {148, 148, 529},
                {200, 200, 219}, {202, 202, 222}, {203, 203, 221},
                {204, 204, 221}, {205, 205, 221}, {223, 223, 10023},
                {225, 225, 218}, {243, 243, 232}, {244, 244, 232},
                {289, 289, 259}, {328, 328, 820},
                {65, 65, 145}, {270, 270, 250}, {271, 271, 249},
                {273, 273, 251}, {275, 275, 254}, {314, 314, 249}, {342, 342, 839},
                {23, 23, 123}, {25, 25, 124},
                {50, 50, 136}, {51, 51, 136}, {52, 52, 136}, {53, 53, 137}, {54, 54, 137},
                {66, 66, 146}, {69, 69, 149}, {92, 92, 506}, {102, 102, 574}, {137, 137, 520},
                {198, 198, 212}, {201, 201, 220}, {208, 208, 220}, {209, 209, 220},
                {213, 213, 223}, {230, 230, 217}, {339, 339, 836},
                {84, 84, 162}, {85, 85, 163}, {101, 101, 604}, {117, 117, 605},
                {133, 133, 606}, {149, 149, 607}, {161, 161, 608}, {173, 173, 609},
                {185, 185, 610}, {262, 262, 300}, {264, 264, 305}, {266, 266, 303},
                {83, 83, 161}, {247, 247, 238}, {259, 259, 811}, {282, 282, 261},
                {284, 284, 261}, {265, 265, 301}, {267, 267, 307}, {326, 326, 164},
                {327, 327, 818, 819}
        };
        for (int[] r : rows) {
            SPRITE_TO_IMGS[r[0]] = Arrays.copyOfRange(r, 2, r.length);
        }
    }

    SpriteAnim(SpriteData data) {
        this.data = data;
        resetDelay();
    }

    static SpriteAnim load(int spriteIndex) {
        SpriteRef ref = SpriteRef.from(spriteIndex);
        String key = ref.sprId + ":" + Arrays.toString(ref.imageIds);
        SpriteData data = CACHE.computeIfAbsent(key, ignored -> SpriteData.load(ref.sprId, ref.imageIds));
        return new SpriteAnim(data);
    }

    static SpriteTable loadSourceSpriteTable() {
        try {
            return SpriteTable.load(AssetPaths.fromWorkingTree(GameConfig.defaultConfig()));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    static final class SpriteRef {
        final int sprId;
        final int[] imageIds;

        SpriteRef(int sprId, int[] imageIds) {
            this.sprId = sprId;
            this.imageIds = imageIds;
        }

        static SpriteRef from(int spriteIndex) {
            if (SOURCE_SPRITE_TABLE != null && spriteIndex >= 0 && spriteIndex < SOURCE_SPRITE_TABLE.size()) {
                int sprId = SOURCE_SPRITE_TABLE.sprId(spriteIndex);
                int[] imageIds = SOURCE_SPRITE_TABLE.imageIds(spriteIndex);
                if (sprId >= 0 && imageIds.length > 0) {
                    return new SpriteRef(sprId, imageIds);
                }
            }
            int[] imageIds = spriteIndex >= 0 && spriteIndex < SPRITE_TO_IMGS.length
                    ? SPRITE_TO_IMGS[spriteIndex]
                    : null;
            if (imageIds == null) {
                imageIds = new int[0];
            }
            return new SpriteRef(spriteIndex, imageIds);
        }
    }

    void setState(int state) {
        if (data.anim != null && state >= 0 && state < data.anim.length) {
            this.state = state;
        } else {
            this.state = 0;
        }
        cursor = 0;
        resetDelay();
    }

    boolean tick() {
        if (data.anim == null || data.anim.length == 0) {
            return false;
        }
        if (delay > 0) {
            delay--;
            return false;
        }
        cursor++;
        boolean completed = false;
        if (cursor >= data.anim[state].length / 2) {
            cursor = 0;
            completed = true;
        }
        resetDelay();
        return completed;
    }

    void tickHoldLast() {
        if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
            return;
        }
        int last = data.anim[state].length / 2 - 1;
        if (cursor >= last) {
            return;
        }
        if (delay > 0) {
            delay--;
            return;
        }
        cursor++;
        resetDelay();
    }

    void resetDelay() {
        if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
            delay = 0;
            return;
        }
        delay = Math.max(0, data.anim[state][cursor * 2] - 1);
    }

    void draw(Graphics2D g, int x, int y, int orientation) {
        if (data.anim == null || data.anim.length == 0 || data.anim[state].length == 0) {
            return;
        }
        int cellId = data.anim[state][cursor * 2 + 1];
        drawCell(g, cellId, x, y, orientation);
    }

    void drawAligned(Graphics2D g, int rectX, int rectY, int rectW, int rectH, int align, int orientation) {
        int[] bounds = animationBounds(state);
        if (bounds == null) {
            return;
        }
        int drawX = rectX;
        int drawY = rectY;
        switch (align) {
            case 4:
                drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                break;
            case 3:
                drawX = rectX - bounds[0];
                drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                break;
            case 5:
                drawX = rectX + (rectW - bounds[2]) - bounds[0];
                drawY = rectY + (rectH - bounds[3]) / 2 - bounds[1];
                break;
            case 6:
                drawX = rectX - bounds[0];
                drawY = rectY + (rectH - bounds[3]) - bounds[1];
                break;
            case 8:
                drawX = rectX + (rectW - bounds[2]) - bounds[0];
                drawY = rectY + (rectH - bounds[3]) - bounds[1];
                break;
            case 7:
                drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                drawY = rectY + (rectH - bounds[3]) - bounds[1];
                break;
            case 2:
                drawX = rectX + (rectW - bounds[2]) - bounds[0];
                drawY = rectY - bounds[1];
                break;
            case 1:
                drawX = rectX + (rectW - bounds[2]) / 2 - bounds[0];
                drawY = rectY - bounds[1];
                break;
            case 0:
            default:
                drawX = rectX - bounds[0];
                drawY = rectY - bounds[1];
                break;
        }
        draw(g, drawX, drawY, orientation);
    }

    int[] cellBounds(int cellId) {
        return data.cellBounds(cellId);
    }

    short[] currentCollisionMask() {
        if (data.anim == null || state < 0 || state >= data.anim.length || data.anim[state].length == 0) {
            return null;
        }
        int cellId = data.anim[state][cursor * 2 + 1];
        return data.collisionMask(cellId);
    }

    short[] currentHitMask() {
        if (data.anim == null || state < 0 || state >= data.anim.length || data.anim[state].length == 0) {
            return null;
        }
        int cellId = data.anim[state][cursor * 2 + 1];
        return data.hitMask(cellId);
    }

    int[] animationBounds(int animState) {
        if (data.anim == null || animState < 0 || animState >= data.anim.length || data.anim[animState].length == 0) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        short[] frames = data.anim[animState];
        for (int i = 0; i < frames.length; i += 2) {
            int[] bounds = data.cellBounds(frames[i + 1]);
            if (bounds == null) {
                continue;
            }
            minX = Math.min(minX, bounds[0]);
            minY = Math.min(minY, bounds[1]);
            maxX = Math.max(maxX, bounds[0] + bounds[2]);
            maxY = Math.max(maxY, bounds[1] + bounds[3]);
        }
        if (minX == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    void drawCell(Graphics2D g, int cellId, int x, int y, int orientation) {
        if (cellId < 0 || cellId >= data.cells.length) {
            return;
        }
        int[] transformMap = orientation == 1
                ? new int[]{2, 4, 1, 7, 0, 5, 3, 6}
                : new int[]{0, 5, 3, 6, 2, 4, 1, 7};
        short[] cells = data.cells[cellId];
        for (int i = 0; i < cells.length; i += 4) {
            int frameId = cells[i];
            if (frameId < 0 || frameId >= data.frames.length) {
                continue;
            }
            int ox = cells[i + 1];
            int oy = cells[i + 2];
            int tr = transformMap[cells[i + 3] & 7];
            if (orientation == 1) {
                int w = data.frames[frameId][3];
                int h = data.frames[frameId][4];
                int adjust = (cells[i + 3] % 2 == 1) ? h : w;
                drawRegion(g, data.imageForFrame(frameId), data.frames[frameId], tr, x - ox - adjust, y + oy);
            } else {
                drawRegion(g, data.imageForFrame(frameId), data.frames[frameId], tr, x + ox, y + oy);
            }
        }
    }

    static void drawRegion(Graphics2D g, BufferedImage img, short[] f, int transform, int x, int y) {
        int sx = f[1], sy = f[2], w = f[3], h = f[4];
        if (w <= 0 || h <= 0) {
            return;
        }
        BufferedImage sub = img.getSubimage(sx, sy, w, h);
        g.drawImage(transformedRegion(sub, transform), x, y, null);
    }

    static BufferedImage transformedRegion(BufferedImage src, int transform) {
        int w = src.getWidth();
        int h = src.getHeight();
        int outW = (transform == 4 || transform == 5 || transform == 6 || transform == 7) ? h : w;
        int outH = (transform == 4 || transform == 5 || transform == 6 || transform == 7) ? w : h;
        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
        for (int dy = 0; dy < outH; dy++) {
            for (int dx = 0; dx < outW; dx++) {
                int sx;
                int sy;
                switch (transform) {
                    case 1: // MIDP TRANS_MIRROR_ROT180
                        sx = dx;
                        sy = h - 1 - dy;
                        break;
                    case 2: // MIDP TRANS_MIRROR
                        sx = w - 1 - dx;
                        sy = dy;
                        break;
                    case 3: // MIDP TRANS_ROT180
                        sx = w - 1 - dx;
                        sy = h - 1 - dy;
                        break;
                    case 4: // MIDP TRANS_MIRROR_ROT270
                        sx = dy;
                        sy = dx;
                        break;
                    case 5: // MIDP TRANS_ROT90
                        sx = dy;
                        sy = h - 1 - dx;
                        break;
                    case 6: // MIDP TRANS_ROT270
                        sx = w - 1 - dy;
                        sy = dx;
                        break;
                    case 7: // MIDP TRANS_MIRROR_ROT90
                        sx = w - 1 - dy;
                        sy = h - 1 - dx;
                        break;
                    case 0:
                    default:
                        sx = dx;
                        sy = dy;
                        break;
                }
                out.setRGB(dx, dy, src.getRGB(sx, sy));
            }
        }
        return out;
    }
}

final class SpriteData {
    final short[][] frames;
    final short[][] cells;
    final short[][] anim;
    final short[][] hitMasks;
    final short[][] collisionMasks;
    final BufferedImage[] images;

    SpriteData(short[][] frames, short[][] cells, short[][] anim,
                       short[][] hitMasks, short[][] collisionMasks, BufferedImage[] images) {
        this.frames = frames;
        this.cells = cells;
        this.anim = anim;
        this.hitMasks = hitMasks;
        this.collisionMasks = collisionMasks;
        this.images = images;
    }

    BufferedImage imageForFrame(int frameId) {
        int slot = frames[frameId][0];
        if (slot < 0 || slot >= images.length) {
            throw new IllegalStateException("Frame " + frameId + " references missing image slot " + slot);
        }
        return images[slot];
    }

    static SpriteData load(int sprId, int[] imageIds) {
        try {
            byte[] bytes = readSpriteBytes(sprId);
            Cursor c = new Cursor();
            short[][] frames = asRows(readFlat(bytes, c));
            short[][] cells = readMatrix(bytes, c);
            short[][] anim = readMatrix(bytes, c);
            if (sprId >= 86 && sprId <= 185) {
                short[][] special = new short[5][4];
                short[] offset = {0, 10, 3, 7, -10};
                for (int i = 0; i < special.length; i++) {
                    for (int j = 0; j < 4; j++) {
                        special[i][j] = j == 1 ? (short) (cells[0][j] + offset[i]) : cells[0][j];
                    }
                }
                cells = special;
                anim = new short[][]{
                        {2, 0},
                        {1, 0, 1, 1, 1, 2, 1, 3, 1, 2},
                        {5, 0, 5, 4}
                };
            }
            short[][] collisionMasks = masksByCell(readFlat(bytes, c), cells.length);
            short[][] hitMasks = masksByCell(readFlat(bytes, c), cells.length);
            if (imageIds == null || imageIds.length == 0) {
                throw new IOException("Missing image mapping for sprite " + sprId);
            }
            BufferedImage[] images = new BufferedImage[imageIds.length];
            for (int i = 0; i < imageIds.length; i++) {
                images[i] = readSpriteImage(imageIds[i]);
                if (images[i] == null) {
                    throw new IOException("Missing image " + imageIds[i] + " for sprite " + sprId);
                }
            }
            return new SpriteData(frames, cells, anim, hitMasks, collisionMasks, images);
        } catch (Exception ex) {
            return blank();
        }
    }

    static SpriteData blank() {
        return new SpriteData(new short[0][0], new short[0][0], new short[0][0],
                null, null, new BufferedImage[0]);
    }

    static byte[] readSpriteBytes(int sprId) throws IOException {
        java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).sprOriginal(sprId);
        if (Files.isRegularFile(path)) {
            return Files.readAllBytes(path);
        }
        return readAll("/spr_" + sprId + "_all(r)");
    }

    static BufferedImage readSpriteImage(int imageId) throws IOException {
        java.nio.file.Path path = AssetPaths.fromWorkingTree(GameConfig.defaultConfig()).imgDecodedPng(imageId);
        if (Files.isRegularFile(path)) {
            return ImageIO.read(path.toFile());
        }
        return ImageIO.read(SpriteData.class.getResource("/img/" + imageId + ".png"));
    }

    int[] cellBounds(int cellId) {
        if (cellId < 0 || cellId >= cells.length || cells[cellId].length == 0) {
            return null;
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        short[] cell = cells[cellId];
        for (int i = 0; i < cell.length; i += 4) {
            int frameId = cell[i];
            if (frameId < 0 || frameId >= frames.length) {
                continue;
            }
            int x = cell[i + 1];
            int y = cell[i + 2];
            int w = frames[frameId][3];
            int h = frames[frameId][4];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + w);
            maxY = Math.max(maxY, y + h);
        }
        if (minX == Integer.MAX_VALUE) {
            return null;
        }
        return new int[]{minX, minY, maxX - minX, maxY - minY};
    }

    short[] collisionMask(int cellId) {
        if (collisionMasks == null || cellId < 0 || cellId >= collisionMasks.length) {
            return null;
        }
        return collisionMasks[cellId];
    }

    short[] hitMask(int cellId) {
        if (hitMasks == null || cellId < 0 || cellId >= hitMasks.length) {
            return null;
        }
        return hitMasks[cellId];
    }

    static short[][] masksByCell(short[] flat, int cellCount) {
        if (flat == null) {
            return null;
        }
        short[][] out = new short[cellCount][];
        for (int i = 0; i + 4 < flat.length; i += 5) {
            int cellId = flat[i];
            if (cellId < 0 || cellId >= out.length) {
                continue;
            }
            short[] rect = new short[]{flat[i + 1], flat[i + 2], flat[i + 3], flat[i + 4]};
            out[cellId] = appendMaskRect(out[cellId], rect);
        }
        return out;
    }

    static short[] appendMaskRect(short[] current, short[] rect) {
        if (current == null) {
            return rect;
        }
        short[] out = Arrays.copyOf(current, current.length + rect.length);
        System.arraycopy(rect, 0, out, current.length, rect.length);
        return out;
    }

    static short[][] asRows(short[] flat) {
        short[][] out = new short[flat.length / 5][5];
        for (int i = 0; i < out.length; i++) {
            System.arraycopy(flat, i * 5, out[i], 0, 5);
        }
        return out;
    }

    static short[] readFlat(byte[] b, Cursor c) {
        int rows = readShort(b, c);
        int cols = readShort(b, c);
        if (rows == 0) {
            return null;
        }
        short[] out = new short[rows * cols];
        for (int i = 0; i < out.length; i++) {
            out[i] = (short) readShort(b, c);
        }
        return out;
    }

    static short[][] readMatrix(byte[] b, Cursor c) {
        int count = readShort(b, c);
        int cols = readShort(b, c);
        if (count == 0) {
            return null;
        }
        short[][] out = new short[count][];
        for (int i = 0; i < count; i++) {
            int len = readShort(b, c);
            out[i] = new short[len * cols];
            for (int j = 0; j < out[i].length; j++) {
                out[i][j] = (short) readShort(b, c);
            }
        }
        return out;
    }

    static int readShort(byte[] b, Cursor c) {
        int v = ((b[c.pos++] & 0xFF) << 8) | (b[c.pos++] & 0xFF);
        return v >= 0x8000 ? v - 0x10000 : v;
    }
    static byte[] readAll(String resource) throws IOException {
        try (InputStream in = SpriteData.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing resource " + resource);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}

final class Cursor {
    int pos;
}


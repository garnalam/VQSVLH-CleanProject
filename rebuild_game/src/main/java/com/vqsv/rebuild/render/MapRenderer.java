package com.vqsv.rebuild.render;

import com.vqsv.rebuild.core.GameConfig;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

public final class MapRenderer {
    private static final int[] TRANSFORM = new int[]{0, 5, 3, 6, 2, 4, 1, 7};

    private final GameMap map;
    private final TileSet tileSet;
    private final DenseLayerCache[] denseLayerCaches;
    private int cameraX;
    private int cameraY;

    public MapRenderer(GameMap map, TileSet tileSet) {
        this.map = map;
        this.tileSet = tileSet;
        this.denseLayerCaches = new DenseLayerCache[map.layerCount()];
    }

    public void centerCameraOn(int worldX, int worldY) {
        setCamera(worldX - GameConfig.LOGICAL_WIDTH / 2, worldY - GameConfig.LOGICAL_HEIGHT / 2);
    }

    public void setCamera(int x, int y) {
        int maxX = Math.max(0, map.widthPixels() - GameConfig.LOGICAL_WIDTH);
        int maxY = Math.max(0, map.heightPixels() - GameConfig.LOGICAL_HEIGHT);
        cameraX = clamp(x, 0, maxX);
        cameraY = clamp(y, 0, maxY);
    }

    public int cameraX() {
        return cameraX;
    }

    public int cameraY() {
        return cameraY;
    }

    public int layerCount() {
        return map.layerCount();
    }

    public boolean hasLayer(int layerIndex) {
        return layerIndex >= 0 && layerIndex < map.layerCount();
    }

    public void render(Graphics2D graphics) {
        for (int layer = 0; layer < map.layerCount(); layer++) {
            renderLayer(graphics, layer);
        }
    }

    public void renderLayer(Graphics2D graphics, int layerIndex) {
        if (!hasLayer(layerIndex)) {
            return;
        }
        int type = map.layerType(layerIndex);
        if (type == 0) {
            renderCachedDenseLayer(graphics, layerIndex);
        } else if (type == 1) {
            renderCachedDenseLayer(graphics, layerIndex);
        } else {
            renderSparseLayer(graphics, map.layer(layerIndex));
        }
    }

    public void renderLayerFull(Graphics2D graphics, int layerIndex) {
        if (!hasLayer(layerIndex)) {
            return;
        }
        int type = map.layerType(layerIndex);
        if (type == 0 || type == 1) {
            renderDenseLayer(graphics, layerIndex);
        } else {
            renderSparseLayer(graphics, map.layer(layerIndex));
        }
    }

    public void renderFull(Graphics2D graphics) {
        for (int layer = 0; layer < map.layerCount(); layer++) {
            renderLayerFull(graphics, layer);
        }
    }

    public void invalidateCache() {
        for (DenseLayerCache cache : denseLayerCaches) {
            if (cache != null) {
                cache.initialized = false;
            }
        }
    }

    private void renderCachedDenseLayer(Graphics2D graphics, int layerIndex) {
        DenseLayerCache cache = denseLayerCaches[layerIndex];
        if (cache == null) {
            cache = new DenseLayerCache();
            denseLayerCaches[layerIndex] = cache;
        }
        if (!cache.initialized) {
            clear(cache.graphics, 0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
            renderDenseLayer(cache.graphics, layerIndex);
            cache.cameraX = cameraX;
            cache.cameraY = cameraY;
            cache.initialized = true;
        } else if (cache.cameraX != cameraX || cache.cameraY != cameraY) {
            int shiftX = cache.cameraX - cameraX;
            int shiftY = cache.cameraY - cameraY;
            if (Math.abs(shiftX) >= GameConfig.LOGICAL_WIDTH || Math.abs(shiftY) >= GameConfig.LOGICAL_HEIGHT) {
                clear(cache.graphics, 0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT);
                renderDenseLayer(cache.graphics, layerIndex);
            } else {
                Composite oldComposite = cache.graphics.getComposite();
                cache.graphics.setComposite(AlphaComposite.Src);
                cache.graphics.copyArea(0, 0, GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, shiftX, shiftY);
                cache.graphics.setComposite(oldComposite);
                redrawExposedStrips(cache.graphics, layerIndex, shiftX, shiftY);
            }
            cache.cameraX = cameraX;
            cache.cameraY = cameraY;
        }
        graphics.drawImage(cache.image, 0, 0, null);
    }

    private void redrawExposedStrips(Graphics2D graphics, int layerIndex, int shiftX, int shiftY) {
        if (shiftX < 0) {
            redrawClipped(graphics, layerIndex, GameConfig.LOGICAL_WIDTH + shiftX, 0, -shiftX, GameConfig.LOGICAL_HEIGHT);
        } else if (shiftX > 0) {
            redrawClipped(graphics, layerIndex, 0, 0, shiftX, GameConfig.LOGICAL_HEIGHT);
        }
        if (shiftY < 0) {
            redrawClipped(graphics, layerIndex, 0, GameConfig.LOGICAL_HEIGHT + shiftY, GameConfig.LOGICAL_WIDTH, -shiftY);
        } else if (shiftY > 0) {
            redrawClipped(graphics, layerIndex, 0, 0, GameConfig.LOGICAL_WIDTH, shiftY);
        }
    }

    private void redrawClipped(Graphics2D graphics, int layerIndex, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        clear(graphics, x, y, width, height);
        Shape oldClip = graphics.getClip();
        graphics.clip(new Rectangle(x, y, width, height));
        try {
            renderDenseLayer(graphics, layerIndex);
        } finally {
            graphics.setClip(oldClip);
        }
    }

    private void renderDenseLayer(Graphics2D graphics, int layerIndex) {
        short[][] layer = map.layer(layerIndex);
        int type = map.layerType(layerIndex);
        int startX = Math.max(0, cameraX / map.tileWidth());
        int startY = Math.max(0, cameraY / map.tileHeight());
        int endX = Math.min(map.widthTiles(), (cameraX + GameConfig.LOGICAL_WIDTH) / map.tileWidth() + 2);
        int endY = Math.min(map.heightTiles(), (cameraY + GameConfig.LOGICAL_HEIGHT) / map.tileHeight() + 2);
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                short raw = layer[x][y];
                if (raw == -1) {
                    continue;
                }
                int tileId = type == 0 ? raw : raw & 0x0FFF;
                int transform = type == 0 ? 0 : (raw & 0x7000) >> 12;
                drawTile(graphics, tileId, transform, x * map.tileWidth() - cameraX, y * map.tileHeight() - cameraY);
            }
        }
    }

    private void renderSparseLayer(Graphics2D graphics, short[][] layer) {
        for (short[] record : layer) {
            int tileId = record[0];
            int tileX = record[1];
            int tileY = record[2];
            int transform = record[3];
            short[] rect = tileSet.rect(tileId);
            int worldX = tileX * map.tileWidth();
            int worldY = tileY * map.tileHeight();
            if (worldX + rect[3] < cameraX || worldY + rect[4] < cameraY
                    || worldX > cameraX + GameConfig.LOGICAL_WIDTH
                    || worldY > cameraY + GameConfig.LOGICAL_HEIGHT) {
                continue;
            }
            drawTile(graphics, tileId, transform, worldX - cameraX, worldY - cameraY);
        }
    }

    private void drawTile(Graphics2D graphics, int tileId, int transform, int x, int y) {
        short[] rect = tileSet.rect(tileId);
        MidpTransform.drawRegion(graphics, tileSet.imageForRect(tileId),
                rect[1], rect[2], rect[3], rect[4], TRANSFORM[transform], x, y);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void clear(Graphics2D graphics, int x, int y, int width, int height) {
        Composite oldComposite = graphics.getComposite();
        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(x, y, width, height);
        graphics.setComposite(oldComposite);
    }

    private static final class DenseLayerCache {
        private final BufferedImage image = new BufferedImage(
                GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        private final Graphics2D graphics = image.createGraphics();
        private boolean initialized;
        private int cameraX;
        private int cameraY;
    }
}

package com.vqsv.rebuild.render;

import com.vqsv.rebuild.core.GameConfig;
import com.vqsv.rebuild.resource.AssetPaths;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class MapSmokeCheck {
    private MapSmokeCheck() {
    }

    public static List<String> run(AssetPaths paths) {
        List<String> lines = new ArrayList<>();
        MapModInfo modInfo = MapModInfo.load(paths);
        GameMap map = GameMap.load(paths, 0);
        TileSet tileSet = TileSet.load(paths, modInfo, map.modId());
        MapRenderer renderer = new MapRenderer(map, tileSet);
        renderer.centerCameraOn(map.widthPixels() / 2, map.heightPixels() / 2);

        lines.add("modInfo=mods:" + modInfo.modCount() + " map0ModImages:" + modInfo.imageIds(map.modId()).length);
        lines.add("map0=mod:" + map.modId()
                + " sizeTiles:" + map.widthTiles() + "x" + map.heightTiles()
                + " sizePixels:" + map.widthPixels() + "x" + map.heightPixels()
                + " layers:" + map.layerCount()
                + " tile:" + map.tileWidth() + "x" + map.tileHeight());
        lines.add("map0LayerTypes=" + layerTypes(map));
        lines.add("map0LayerCounts=" + layerCounts(map));
        lines.add("mod" + map.modId() + "=images:" + tileSet.imageCount() + " rects:" + tileSet.rectCount());

        BufferedImage canvas = new BufferedImage(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        try {
            renderer.render(graphics);
        } finally {
            graphics.dispose();
        }
        lines.add("map0Draw=samplePixels:" + countNonTransparentPixels(canvas)
                + " camera:" + renderer.cameraX() + "," + renderer.cameraY());
        for (int layer = 0; layer < map.layerCount(); layer++) {
            int type = map.layerType(layer);
            if (type == 0 || type == 1) {
                lines.add(checkDenseCopyArea(paths, map, tileSet, layer));
            }
        }
        return lines;
    }

    private static String checkDenseCopyArea(AssetPaths paths, GameMap map, TileSet tileSet, int layerIndex) {
        if (layerIndex >= map.layerCount()) {
            return "mapCopyAreaDenseLayer" + layerIndex + "=skipped:missingLayer";
        }
        int type = map.layerType(layerIndex);
        if (type != 0 && type != 1) {
            return "mapCopyAreaDenseLayer" + layerIndex + "=skipped:type" + type;
        }

        MapRenderer cached = new MapRenderer(map, tileSet);
        MapRenderer full = new MapRenderer(GameMap.load(paths, map.mapId()), tileSet);
        int[][] cameras = new int[][]{
                {0, 0},
                {16, 0},
                {40, 24},
                {64, 24},
                {32, 48},
                {0, 24}
        };
        BufferedImage previousFullImage = null;
        int previousCameraX = 0;
        int previousCameraY = 0;
        for (int index = 0; index < cameras.length; index++) {
            cached.setCamera(cameras[index][0], cameras[index][1]);
            full.setCamera(cameras[index][0], cameras[index][1]);
            BufferedImage cachedImage = renderLayer(cached, layerIndex, false);
            BufferedImage fullImage = renderLayer(full, layerIndex, true);
            int mismatch = firstMismatch(cachedImage, fullImage);
            if (mismatch >= 0) {
                int x = mismatch % GameConfig.LOGICAL_WIDTH;
                int y = mismatch / GameConfig.LOGICAL_WIDTH;
                return "mapCopyAreaDenseLayer" + layerIndex + "=FAILED cameraIndex:" + index
                        + " pixel:" + x + "," + y
                        + " cached:" + Integer.toHexString(cachedImage.getRGB(x, y))
                        + " full:" + Integer.toHexString(fullImage.getRGB(x, y))
                        + previousPixelProbe(previousFullImage, previousCameraX, previousCameraY,
                        cameras[index][0], cameras[index][1], x, y, cachedImage.getRGB(x, y));
            }
            previousFullImage = fullImage;
            previousCameraX = cameras[index][0];
            previousCameraY = cameras[index][1];
        }
        return "mapCopyAreaDenseLayer" + layerIndex + "=verified cameras:" + cameras.length;
    }

    private static String previousPixelProbe(BufferedImage previous, int previousCameraX, int previousCameraY,
                                             int cameraX, int cameraY, int x, int y, int cachedRgb) {
        if (previous == null) {
            return "";
        }
        int previousX = x + cameraX - previousCameraX;
        int previousY = y + cameraY - previousCameraY;
        if (previousX < 0 || previousY < 0 || previousX >= previous.getWidth() || previousY >= previous.getHeight()) {
            return " previousExpected:outside";
        }
        return " previousExpected:" + Integer.toHexString(previous.getRGB(previousX, previousY))
                + " previousAtSame:" + Integer.toHexString(previous.getRGB(x, y))
                + " cachedPreviousMatchX:" + firstMatchingX(previous, cachedRgb, previousY);
    }

    private static int firstMatchingX(BufferedImage image, int rgb, int y) {
        for (int x = 0; x < image.getWidth(); x++) {
            if (image.getRGB(x, y) == rgb) {
                return x;
            }
        }
        return -1;
    }

    private static BufferedImage renderLayer(MapRenderer renderer, int layerIndex, boolean full) {
        BufferedImage image = new BufferedImage(GameConfig.LOGICAL_WIDTH, GameConfig.LOGICAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            if (full) {
                renderer.renderLayerFull(graphics, layerIndex);
            } else {
                renderer.renderLayer(graphics, layerIndex);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private static int firstMismatch(BufferedImage left, BufferedImage right) {
        for (int y = 0; y < left.getHeight(); y++) {
            for (int x = 0; x < left.getWidth(); x++) {
                if (left.getRGB(x, y) != right.getRGB(x, y)) {
                    return y * left.getWidth() + x;
                }
            }
        }
        return -1;
    }

    private static String layerTypes(GameMap map) {
        StringBuilder builder = new StringBuilder("[");
        for (int layer = 0; layer < map.layerCount(); layer++) {
            if (layer > 0) {
                builder.append(',');
            }
            builder.append(map.layerType(layer));
        }
        return builder.append(']').toString();
    }

    private static String layerCounts(GameMap map) {
        StringBuilder builder = new StringBuilder("[");
        for (int layer = 0; layer < map.layerCount(); layer++) {
            if (layer > 0) {
                builder.append(',');
            }
            builder.append(map.layerRecordCount(layer));
        }
        return builder.append(']').toString();
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

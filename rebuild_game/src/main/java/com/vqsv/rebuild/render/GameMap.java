package com.vqsv.rebuild.render;

import com.vqsv.rebuild.resource.AssetPaths;
import com.vqsv.rebuild.resource.BinaryReader;
import com.vqsv.rebuild.resource.ResourceException;
import com.vqsv.rebuild.resource.ResourceLocator;

public final class GameMap {
    private final int mapId;
    private final int modId;
    private final int widthTiles;
    private final int heightTiles;
    private final int tileWidth;
    private final int tileHeight;
    private final byte[] layerTypes;
    private final short[][][] layers;

    private GameMap(int mapId, int modId, int widthTiles, int heightTiles, int tileSize,
                    byte[] layerTypes, short[][][] layers) {
        this.mapId = mapId;
        this.modId = modId;
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
        this.tileWidth = tileSize;
        this.tileHeight = tileSize;
        this.layerTypes = layerTypes;
        this.layers = layers;
    }

    public static GameMap load(AssetPaths paths, int mapId) {
        BinaryReader reader = new ResourceLocator(paths).binary(paths.mapOriginal(mapId));
        int compactFlag = reader.readByte();
        int modId = reader.readByte();
        int widthTiles = compactFlag == 1 ? reader.readByte() : reader.readShort();
        int heightTiles = compactFlag == 1 ? reader.readByte() : reader.readShort();
        int tileSize = reader.readByte();
        int layerCount = reader.readByte();
        byte[] layerTypes = new byte[layerCount];
        short[][][] layers = new short[layerCount][][];

        for (int order = 0; order < layerCount; order++) {
            int layerIndex = reader.readByte();
            int layerType = reader.readByte();
            int recordCount = reader.readShort();
            layerTypes[layerIndex] = (byte) layerType;
            layers[layerIndex] = createLayer(layerType, recordCount, widthTiles, heightTiles);

            for (int record = 0; record < recordCount; record++) {
                int x = compactFlag == 1 ? reader.readByte() : reader.readShort();
                int y = compactFlag == 1 ? reader.readByte() : reader.readShort();
                short rawTile = reader.readShort();
                if (layerType == 1) {
                    layers[layerIndex][x][y] = rawTile;
                } else if (layerType == 0) {
                    layers[layerIndex][x][y] = (short) (rawTile & 0x0FFF);
                } else {
                    layers[layerIndex][record][0] = (short) (rawTile & 0x0FFF);
                    layers[layerIndex][record][1] = (short) x;
                    layers[layerIndex][record][2] = (short) y;
                    layers[layerIndex][record][3] = (short) ((rawTile & 0x7000) >> 12);
                }
            }
        }

        return new GameMap(mapId, modId, widthTiles, heightTiles, tileSize, layerTypes, layers);
    }

    public int mapId() {
        return mapId;
    }

    public int modId() {
        return modId;
    }

    public int widthTiles() {
        return widthTiles;
    }

    public int heightTiles() {
        return heightTiles;
    }

    public int widthPixels() {
        return widthTiles * tileWidth;
    }

    public int heightPixels() {
        return heightTiles * tileHeight;
    }

    public int tileWidth() {
        return tileWidth;
    }

    public int tileHeight() {
        return tileHeight;
    }

    public int layerCount() {
        return layers.length;
    }

    public int layerType(int layerIndex) {
        return layerTypes[layerIndex];
    }

    public short[][] layer(int layerIndex) {
        return layers[layerIndex];
    }

    public int layerRecordCount(int layerIndex) {
        int type = layerType(layerIndex);
        if (type == 0 || type == 1) {
            int count = 0;
            short[][] layer = layers[layerIndex];
            for (int x = 0; x < layer.length; x++) {
                for (int y = 0; y < layer[x].length; y++) {
                    if (layer[x][y] != -1) {
                        count++;
                    }
                }
            }
            return count;
        }
        return layers[layerIndex].length;
    }

    private static short[][] createLayer(int layerType, int recordCount, int widthTiles, int heightTiles) {
        if (layerType == 0 || layerType == 1) {
            short[][] layer = new short[widthTiles][heightTiles];
            for (int x = 0; x < widthTiles; x++) {
                for (int y = 0; y < heightTiles; y++) {
                    layer[x][y] = -1;
                }
            }
            return layer;
        }
        if (layerType == 2 || layerType == 3 || layerType == 4) {
            return new short[recordCount][4];
        }
        throw new ResourceException("Unsupported map layer type: " + layerType);
    }
}

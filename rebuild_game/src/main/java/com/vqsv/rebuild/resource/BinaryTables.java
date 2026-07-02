package com.vqsv.rebuild.resource;

public final class BinaryTables {
    private BinaryTables() {
    }

    public static short[][] readShortRows(BinaryReader reader) {
        int rowCount = reader.readShort();
        if (rowCount < 0) {
            throw new ResourceException("Negative row count in short table: " + rowCount);
        }
        short[][] rows = new short[rowCount][];
        for (int row = 0; row < rowCount; row++) {
            int length = reader.readShort();
            if (length < 0) {
                throw new ResourceException("Negative row length in short table row " + row + ": " + length);
            }
            rows[row] = new short[length];
            for (int index = 0; index < length; index++) {
                rows[row][index] = reader.readShort();
            }
        }
        return rows;
    }

    public static short[] readPackedFlatShorts(BinaryReader reader) {
        int rows = reader.readUnsignedShort();
        int width = reader.readUnsignedShort();
        if (rows == 0) {
            return null;
        }
        short[] values = new short[rows * width];
        for (int index = 0; index < values.length; index++) {
            values[index] = reader.readShort();
        }
        return values;
    }

    public static short[][] readPackedShortRows(BinaryReader reader) {
        int rowCount = reader.readUnsignedShort();
        int width = reader.readUnsignedShort();
        if (rowCount == 0) {
            return null;
        }
        short[][] rows = new short[rowCount][];
        for (int row = 0; row < rowCount; row++) {
            int length = reader.readUnsignedShort();
            rows[row] = new short[length * width];
            for (int index = 0; index < rows[row].length; index++) {
                rows[row][index] = reader.readShort();
            }
        }
        return rows;
    }

    public static short[] append(short[] current, short[] extra) {
        int currentLength = current == null ? 0 : current.length;
        short[] result = new short[currentLength + extra.length];
        if (current != null) {
            System.arraycopy(current, 0, result, 0, current.length);
        }
        System.arraycopy(extra, 0, result, currentLength, extra.length);
        return result;
    }
}

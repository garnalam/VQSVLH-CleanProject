package com.vqsv.rebuild.resource;

import java.nio.file.Path;
import java.util.Arrays;

public final class BinaryReader {
    private final byte[] data;
    private final String sourceName;
    private int position;

    public BinaryReader(byte[] data, String sourceName) {
        this.data = Arrays.copyOf(data, data.length);
        this.sourceName = sourceName;
    }

    public static BinaryReader of(byte[] data, Path source) {
        return new BinaryReader(data, source.toString());
    }

    public int length() {
        return data.length;
    }

    public int position() {
        return position;
    }

    public int remaining() {
        return data.length - position;
    }

    public boolean hasRemaining() {
        return remaining() > 0;
    }

    public void seek(int newPosition) {
        if (newPosition < 0 || newPosition > data.length) {
            throw error("seek outside file: " + newPosition);
        }
        position = newPosition;
    }

    public void skip(int count) {
        seek(position + count);
    }

    public byte readByte() {
        require(1);
        return data[position++];
    }

    public int readUnsignedByte() {
        return readByte() & 0xFF;
    }

    public short readShort() {
        return (short) readUnsignedShort();
    }

    public int readUnsignedShort() {
        require(2);
        int value = ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
        position += 2;
        return value;
    }

    public int readInt() {
        require(4);
        int value = ((data[position] & 0xFF) << 24)
                | ((data[position + 1] & 0xFF) << 16)
                | ((data[position + 2] & 0xFF) << 8)
                | (data[position + 3] & 0xFF);
        position += 4;
        return value;
    }

    public long readUnsignedInt() {
        return readInt() & 0xFFFFFFFFL;
    }

    public byte[] readBytes(int count) {
        require(count);
        byte[] value = Arrays.copyOfRange(data, position, position + count);
        position += count;
        return value;
    }

    public BinaryReader slice(int count, String label) {
        return new BinaryReader(readBytes(count), sourceName + ":" + label);
    }

    private void require(int count) {
        if (count < 0) {
            throw error("negative read size: " + count);
        }
        if (remaining() < count) {
            throw error("unexpected EOF at " + position + ", need " + count + " bytes, remaining " + remaining());
        }
    }

    private ResourceException error(String message) {
        return new ResourceException(sourceName + ": " + message);
    }
}

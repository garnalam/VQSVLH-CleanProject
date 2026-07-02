package com.vqsv.rebuild.resource;

public final class BinaryReaderSelfTest {
    private BinaryReaderSelfTest() {
    }

    public static String run() {
        BinaryReader reader = new BinaryReader(new byte[]{
                (byte) 0xFE,
                0x01,
                0x23,
                (byte) 0x80,
                0x00,
                0x00,
                0x02
        }, "self-test");

        int unsignedByte = reader.readUnsignedByte();
        int unsignedShort = reader.readUnsignedShort();
        int signedInt = reader.readInt();

        if (unsignedByte != 254) {
            throw new ResourceException("BinaryReader self-test failed: unsigned byte");
        }
        if (unsignedShort != 0x0123) {
            throw new ResourceException("BinaryReader self-test failed: unsigned short");
        }
        if (signedInt != 0x80000002) {
            throw new ResourceException("BinaryReader self-test failed: signed int");
        }
        if (reader.remaining() != 0) {
            throw new ResourceException("BinaryReader self-test failed: remaining bytes");
        }
        return "binaryReaderSelfTest=ok bigEndian=true";
    }
}

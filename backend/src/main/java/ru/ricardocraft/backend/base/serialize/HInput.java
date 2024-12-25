package ru.ricardocraft.backend.base.serialize;

import ru.ricardocraft.backend.base.helper.IOHelper;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class HInput implements AutoCloseable {

    public final InputStream stream;


    public HInput(InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }


    public boolean readBoolean() throws IOException {
        int b = readUnsignedByte();
        return switch (b) {
            case 0b0 -> false;
            case 0b1 -> true;
            default -> throw new IOException("Invalid boolean state: " + b);
        };
    }


    public byte[] readByteArray(int max) throws IOException {
        byte[] bytes = new byte[readLength(max)];
        IOHelper.read(stream, bytes);
        return bytes;
    }


    public int readLength(int max) throws IOException {
        if (max < 0)
            return -max;
        return IOHelper.verifyLength(readVarInt(), max);
    }


    public String readString(int maxBytes) throws IOException {
        return IOHelper.decode(readByteArray(maxBytes));
    }


    public int readUnsignedByte() throws IOException {
        int b = stream.read();
        if (b < 0)
            throw new EOFException("readUnsignedByte");
        return b;
    }

    public int readVarInt() throws IOException {
        int shift = 0;
        int result = 0;
        while (shift < Integer.SIZE) {
            int b = readUnsignedByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0)
                return result;
            shift += 7;
        }
        throw new IOException("VarInt too big");
    }


    public long readVarLong() throws IOException {
        int shift = 0;
        long result = 0;
        while (shift < Long.SIZE) {
            int b = readUnsignedByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0)
                return result;
            shift += 7;
        }
        throw new IOException("VarLong too big");
    }
}

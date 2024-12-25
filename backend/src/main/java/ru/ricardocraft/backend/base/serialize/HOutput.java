package ru.ricardocraft.backend.base.serialize;

import ru.ricardocraft.backend.base.helper.IOHelper;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public final class HOutput implements AutoCloseable, Flushable {

    public final OutputStream stream;


    public HOutput(OutputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }


    public void writeASCII(String s, int maxBytes) throws IOException {
        writeByteArray(IOHelper.encodeASCII(s), maxBytes);
    }


    public void writeBoolean(boolean b) throws IOException {
        writeUnsignedByte(b ? 0b1 : 0b0);
    }


    public void writeByteArray(byte[] bytes, int max) throws IOException {
        writeLength(bytes.length, max);
        stream.write(bytes);
    }


    public void writeLength(int length, int max) throws IOException {
        IOHelper.verifyLength(length, max);
        if (max >= 0)
            writeVarInt(length);
    }


    public void writeString(String s, int maxBytes) throws IOException {
        writeByteArray(IOHelper.encode(s), maxBytes);
    }


    public void writeUnsignedByte(int b) throws IOException {
        stream.write(b);
    }


    public void writeVarInt(int i) throws IOException {
        while ((i & ~0x7FL) != 0) {
            writeUnsignedByte(i & 0x7F | 0x80);
            i >>>= 7;
        }
        writeUnsignedByte(i);
    }


    public void writeVarLong(long l) throws IOException {
        while ((l & ~0x7FL) != 0) {
            writeUnsignedByte((int) l & 0x7F | 0x80);
            l >>>= 7;
        }
        writeUnsignedByte((int) l);
    }
}

package ru.ricardocraft.backend.binary.tasks.sign;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Helper output stream that also sends the data to the given.
 */
public class HashingOutputStream extends OutputStream {
    public final OutputStream out;
    public final MessageDigest hasher;

    public HashingOutputStream(OutputStream out, MessageDigest hasher) {
        this.out = out;
        this.hasher = hasher;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        hasher.update(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        hasher.update(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        hasher.update((byte) b);
    }

    public byte[] digest() {
        return hasher.digest();
    }
}
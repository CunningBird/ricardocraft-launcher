package ru.ricardocraft.backend.binary.tasks.sign;

import java.io.OutputStream;
import java.security.MessageDigest;


/**
 * Helper output stream that also sends the data to the given.
 */
public class HashingNonClosingOutputStream extends HashingOutputStream {

    public HashingNonClosingOutputStream(OutputStream out, MessageDigest hasher) {
        super(out, hasher);
    }

    @Override
    public void close() {
        // Do nothing
    }
}

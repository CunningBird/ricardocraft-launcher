package ru.ricardocraft.client.core.hasher;

import ru.ricardocraft.client.core.LauncherNetworkAPI;
import ru.ricardocraft.client.core.serialize.HInput;
import ru.ricardocraft.client.core.serialize.HOutput;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper.DigestAlgorithm;
import ru.ricardocraft.client.utils.helper.VerifyHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public final class HashedFile extends HashedEntry {
    public static final DigestAlgorithm DIGEST_ALGO = DigestAlgorithm.SHA1;

    // Instance
    @LauncherNetworkAPI
    public final long size;
    @LauncherNetworkAPI
    private final byte[] digest;


    public HashedFile(HInput input) throws IOException {
        this(input.readVarLong(), input.readBoolean() ? input.readByteArray(-DIGEST_ALGO.bytes) : null);
    }


    public HashedFile(long size, byte[] digest) {
        this.size = VerifyHelper.verifyLong(size, VerifyHelper.L_NOT_NEGATIVE, "Illegal size: " + size);
        this.digest = digest == null ? null : DIGEST_ALGO.verify(digest).clone();
    }


    public HashedFile(Path file, long size, boolean digest) throws IOException {
        this(size, digest ? SecurityHelper.digest(DIGEST_ALGO, file) : null);
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }


    public boolean isNotSame(HashedFile o) {
        return size != o.size || (digest != null && o.digest != null && !Arrays.equals(digest, o.digest));
    }

    public boolean isSame(Path file, boolean digest) throws IOException {
        if (size != IOHelper.readAttributes(file).size())
            return false;
        if (!digest || this.digest == null)
            return true;

        // Create digest
        byte[] actualDigest = SecurityHelper.digest(DIGEST_ALGO, file);
        return Arrays.equals(this.digest, actualDigest);
    }

    @Override
    public long size() {
        return size;
    }

    public byte[] getDigest() {
        return digest;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarLong(size);
        output.writeBoolean(digest != null);
        if (digest != null)
            output.writeByteArray(digest, -DIGEST_ALGO.bytes);
    }
}

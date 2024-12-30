package ru.ricardocraft.backend.base.hasher;

import lombok.Getter;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper.DigestAlgorithm;
import ru.ricardocraft.backend.base.helper.VerifyHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.LongPredicate;

@Getter
public final class HashedFile extends HashedEntry {
    public static final DigestAlgorithm DIGEST_ALGO = DigestAlgorithm.SHA1;

    // Instance
    public final long size;
    private final byte[] digest;

    public HashedFile(HInput input) throws IOException {
        this(input.readVarLong(), input.readBoolean() ? input.readByteArray(-DIGEST_ALGO.bytes) : null);
    }

    public HashedFile(long size, byte[] digest) {
        this.size = verifyLong(size, VerifyHelper.L_NOT_NEGATIVE, "Illegal size: " + size);
        this.digest = digest == null ? null : DIGEST_ALGO.verify(digest).clone();
    }

    public HashedFile(Path file, long size, boolean digest) throws IOException {
        this(size, digest ? SecurityHelper.digest(DIGEST_ALGO, file) : null);
    }

    @Override
    public Type getType() {
        return Type.FILE;
    }


    private long verifyLong(long l, LongPredicate predicate, String error) {
        if (predicate.test(l))
            return l;
        throw new IllegalArgumentException(error);
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarLong(size);
        output.writeBoolean(digest != null);
        if (digest != null)
            output.writeByteArray(digest, -DIGEST_ALGO.bytes);
    }
}

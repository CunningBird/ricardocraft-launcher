package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class VerifySecureLevelKeyResponse extends SimpleResponse {
    public byte[] publicKey;
    public byte[] signature;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

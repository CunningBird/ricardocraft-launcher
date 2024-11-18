package ru.ricardocraft.backend.dto.secure;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class VerifySecureLevelKeyResponse extends SimpleResponse {
    public byte[] publicKey;
    public byte[] signature;

    @Override
    public String getType() {
        return "verifySecureLevelKey";
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

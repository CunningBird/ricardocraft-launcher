package ru.ricardocraft.backend.socket.response.secure;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

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

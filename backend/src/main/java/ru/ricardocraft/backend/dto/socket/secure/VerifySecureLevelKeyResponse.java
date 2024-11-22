package ru.ricardocraft.backend.dto.socket.secure;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class VerifySecureLevelKeyResponse extends SimpleResponse {
    public byte[] publicKey;
    public byte[] signature;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

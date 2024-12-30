package ru.ricardocraft.backend.dto.request.secure;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class VerifySecureLevelKeyRequest extends AbstractRequest {
    public byte[] publicKey;
    public byte[] signature;
}

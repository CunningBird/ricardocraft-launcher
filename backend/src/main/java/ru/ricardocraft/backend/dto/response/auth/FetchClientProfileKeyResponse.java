package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.security.PrivateKey;
import java.security.PublicKey;

public class FetchClientProfileKeyResponse extends AbstractResponse {
    public byte[] publicKey;
    public byte[] privateKey;
    public byte[] signature /* V2 */;
    public long expiresAt;
    public long refreshedAfter;

    public FetchClientProfileKeyResponse(PublicKey publicKey, PrivateKey privateKey, byte[] signature, long expiresAt, long refreshedAfter) {
        this.publicKey = publicKey.getEncoded();
        this.privateKey = privateKey.getEncoded();
        this.signature = signature;
        this.expiresAt = expiresAt;
        this.refreshedAfter = refreshedAfter;
    }

    @Override
    public String getType() {
        return "clientProfileKey";
    }
}

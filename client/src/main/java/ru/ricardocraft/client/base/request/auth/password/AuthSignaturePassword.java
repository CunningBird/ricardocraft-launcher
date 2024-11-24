package ru.ricardocraft.client.base.request.auth.password;

import ru.ricardocraft.client.base.request.auth.AuthRequest;

public class AuthSignaturePassword implements AuthRequest.AuthPasswordInterface {
    public byte[] signature;
    public byte[] publicKey;
    public byte[] salt;

    public AuthSignaturePassword(byte[] signature, byte[] publicKey, byte[] salt) {
        this.signature = signature;
        this.publicKey = publicKey;
        this.salt = salt;
    }

    @Override
    public boolean check() {
        return true;
    }
}

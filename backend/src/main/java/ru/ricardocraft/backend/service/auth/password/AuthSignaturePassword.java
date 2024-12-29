package ru.ricardocraft.backend.service.auth.password;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthSignaturePassword extends AuthPassword {

    public byte[] signature;
    public byte[] publicKey;
    public byte[] salt;

    @Override
    public boolean check() {
        return true;
    }
}

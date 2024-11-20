package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

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

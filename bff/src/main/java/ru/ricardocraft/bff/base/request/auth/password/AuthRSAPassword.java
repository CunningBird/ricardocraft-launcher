package ru.ricardocraft.bff.base.request.auth.password;

import ru.ricardocraft.bff.base.request.auth.AuthRequest;

public class AuthRSAPassword implements AuthRequest.AuthPasswordInterface {
    public final byte[] password;

    public AuthRSAPassword(byte[] rsaEncryptedPassword) {
        this.password = rsaEncryptedPassword;
    }

    @Override
    public boolean check() {
        return true;
    }
}

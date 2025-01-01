package ru.ricardocraft.client.dto.request.auth.password;

import ru.ricardocraft.client.dto.request.auth.AuthRequest;

public class AuthAESPassword implements AuthRequest.AuthPasswordInterface {

    public final byte[] password;

    public AuthAESPassword(byte[] aesEncryptedPassword) {
        this.password = aesEncryptedPassword;
    }

    @Override
    public boolean check() {
        return true;
    }
}

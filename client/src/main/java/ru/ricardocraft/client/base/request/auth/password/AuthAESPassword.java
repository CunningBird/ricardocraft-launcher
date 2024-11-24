package ru.ricardocraft.client.base.request.auth.password;

import ru.ricardocraft.client.base.request.auth.AuthRequest;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

public class AuthAESPassword implements AuthRequest.AuthPasswordInterface {
    @LauncherNetworkAPI
    public final byte[] password;

    public AuthAESPassword(byte[] aesEncryptedPassword) {
        this.password = aesEncryptedPassword;
    }

    @Override
    public boolean check() {
        return true;
    }
}

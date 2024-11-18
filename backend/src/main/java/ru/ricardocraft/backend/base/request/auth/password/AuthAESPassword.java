package ru.ricardocraft.backend.base.request.auth.password;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;

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

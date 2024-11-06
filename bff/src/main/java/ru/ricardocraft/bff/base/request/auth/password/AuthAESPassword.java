package ru.ricardocraft.bff.base.request.auth.password;

import ru.ricardocraft.bff.base.request.auth.AuthRequest;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

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

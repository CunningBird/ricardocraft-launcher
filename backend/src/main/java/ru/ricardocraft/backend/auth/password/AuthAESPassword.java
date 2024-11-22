package ru.ricardocraft.backend.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;

@NoArgsConstructor
public class AuthAESPassword extends AuthPassword {

    @LauncherNetworkAPI
    public byte[] password;

    public AuthAESPassword(byte[] aesEncryptedPassword) {
        this.password = aesEncryptedPassword;
    }

    @Override
    public boolean check() {
        return true;
    }
}

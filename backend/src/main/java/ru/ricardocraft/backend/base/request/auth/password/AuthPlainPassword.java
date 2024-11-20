package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class AuthPlainPassword extends AuthPassword {

    @LauncherNetworkAPI
    public String password;

    public AuthPlainPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean check() {
        return true;
    }
}

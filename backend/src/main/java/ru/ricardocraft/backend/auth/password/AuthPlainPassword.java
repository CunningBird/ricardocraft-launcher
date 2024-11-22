package ru.ricardocraft.backend.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;

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

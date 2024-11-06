package ru.ricardocraft.bff.base.request.auth.password;

import ru.ricardocraft.bff.base.request.auth.AuthRequest;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

public class AuthPlainPassword implements AuthRequest.AuthPasswordInterface {
    @LauncherNetworkAPI
    public final String password;

    public AuthPlainPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean check() {
        return true;
    }
}

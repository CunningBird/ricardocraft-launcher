package ru.ricardocraft.client.base.request.auth.password;

import ru.ricardocraft.client.base.request.auth.AuthRequest;
import ru.ricardocraft.client.core.LauncherNetworkAPI;

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

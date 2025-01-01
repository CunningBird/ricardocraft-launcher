package ru.ricardocraft.client.dto.request.auth.password;

import ru.ricardocraft.client.dto.request.auth.AuthRequest;

public class AuthPlainPassword implements AuthRequest.AuthPasswordInterface {

    public final String password;

    public AuthPlainPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean check() {
        return true;
    }
}

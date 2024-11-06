package ru.ricardocraft.backend.base.request.auth.password;

import ru.ricardocraft.backend.base.request.auth.AuthRequest;

public class AuthCodePassword implements AuthRequest.AuthPasswordInterface {
    public final String uri;

    public AuthCodePassword(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean check() {
        return true;
    }
}

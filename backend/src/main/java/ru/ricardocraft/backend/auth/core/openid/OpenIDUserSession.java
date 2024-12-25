package ru.ricardocraft.backend.auth.core.openid;

import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.repository.User;

record OpenIDUserSession(User user, String token, long expiresIn) implements UserSession {

    @Override
    public String getID() {
        return user.getUsername();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public String getMinecraftAccessToken() {
        return token;
    }

    @Override
    public long getExpireIn() {
        return expiresIn;
    }
}
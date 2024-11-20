package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.base.request.auth.AuthPassword;
import ru.ricardocraft.backend.dto.SimpleResponse;

public class AuthResponse extends SimpleResponse {

    public String login;
    public String client;
    public AuthPassword password;
    public Boolean getSession;
    public String auth_id;
    public ConnectTypes authType;

    public enum ConnectTypes {
        CLIENT,
        API
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

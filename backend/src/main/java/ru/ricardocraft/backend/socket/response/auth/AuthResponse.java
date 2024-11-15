package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class AuthResponse extends SimpleResponse {

    public String login;
    public String client;

    public AuthRequest.AuthPasswordInterface password;

    public String auth_id;
    public ConnectTypes authType;

    @Override
    public String getType() {
        return "auth";
    }

    public enum ConnectTypes {
        CLIENT,
        API
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

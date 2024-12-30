package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.service.auth.password.AuthPassword;
import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class AuthRequest extends AbstractRequest {

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
}

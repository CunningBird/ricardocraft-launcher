package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.WebSocketRequest;
import ru.ricardocraft.backend.base.request.auth.password.AuthPlainPassword;

public final class AuthRequest extends Request<AuthRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String login;
    @LauncherNetworkAPI
    public final AuthPasswordInterface password;
    @LauncherNetworkAPI
    public final String auth_id;
    @LauncherNetworkAPI
    public final boolean getSession;
    @LauncherNetworkAPI
    public final ConnectTypes authType;

    public AuthRequest(String login, String password, String auth_id, ConnectTypes authType) {
        this.login = login;
        this.password = new AuthPlainPassword(password);
        this.auth_id = auth_id;
        this.authType = authType;
        this.getSession = false;
    }

    public AuthRequest(String login, AuthPasswordInterface password, String auth_id, boolean getSession, ConnectTypes authType) {
        this.login = login;
        this.password = password;
        this.auth_id = auth_id;
        this.getSession = getSession;
        this.authType = authType;
    }

    @Override
    public String getType() {
        return "auth";
    }

    public enum ConnectTypes {
        @LauncherNetworkAPI
        CLIENT,
        @LauncherNetworkAPI
        API
    }

    public interface AuthPasswordInterface {
        boolean check();

        default boolean isAllowSave() {
            return false;
        }
    }
}

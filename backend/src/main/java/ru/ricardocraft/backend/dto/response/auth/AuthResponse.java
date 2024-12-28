package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.profiles.PlayerProfile;

import java.util.UUID;

public class AuthResponse extends AbstractResponse {
    public static final String TWO_FACTOR_NEED_ERROR_MESSAGE = "auth.require2fa";
    public static final String ONE_FACTOR_NEED_ERROR_MESSAGE_PREFIX = "auth.require.factor.";
    public static final String OAUTH_TOKEN_EXPIRE = "auth.expiretoken";
    public static final String OAUTH_TOKEN_INVALID = "auth.invalidtoken";
    public static final String USER_NOT_FOUND_ERROR_MESSAGE = "auth.usernotfound";
    public static final String WRONG_PASSWORD_ERROR_MESSAGE = "auth.wrongpassword";
    public static final String ACCOUNT_BLOCKED_ERROR_MESSAGE = "auth.userblocked";
    @LauncherNetworkAPI
    public ClientPermissions permissions;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;
    @LauncherNetworkAPI
    public String accessToken;
    @LauncherNetworkAPI
    public String protectToken;
    @LauncherNetworkAPI
    @Deprecated // Always null
    public UUID session;
    @LauncherNetworkAPI
    public OAuthRequestEvent oauth;

    public AuthResponse() {
    }

    public AuthResponse(PlayerProfile pp, String accessToken, ClientPermissions permissions) {
        this.playerProfile = pp;
        this.accessToken = accessToken;
        this.permissions = permissions;
    }

    public AuthResponse(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
    }

    public AuthResponse(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken, UUID session) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
        this.session = session;
    }

    public AuthResponse(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken, UUID session, OAuthRequestEvent oauth) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
        this.session = session;
        this.oauth = oauth;
    }

    @Override
    public String getType() {
        return "auth";
    }

    public static class OAuthRequestEvent {
        public final String accessToken;
        public final String refreshToken;
        public final long expire;

        public OAuthRequestEvent(String accessToken, String refreshToken, long expire) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expire = expire;
        }
    }
}

package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.service.ClientPermissions;
import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.PlayerProfile;

import java.util.UUID;

public class AuthRequestEvent extends RequestEvent {
    public static final String TWO_FACTOR_NEED_ERROR_MESSAGE = "auth.require2fa";
    public static final String ONE_FACTOR_NEED_ERROR_MESSAGE_PREFIX = "auth.require.factor.";
    public static final String OAUTH_TOKEN_EXPIRE = "auth.expiretoken";
    public static final String OAUTH_TOKEN_INVALID = "auth.invalidtoken";

    public ClientPermissions permissions;
    public PlayerProfile playerProfile;
    public String accessToken;
    public String protectToken;
    @Deprecated // Always null
    public UUID session;
    public OAuthRequestEvent oauth;

    public AuthRequestEvent() {
    }

    public AuthRequestEvent(PlayerProfile pp, String accessToken, ClientPermissions permissions) {
        this.playerProfile = pp;
        this.accessToken = accessToken;
        this.permissions = permissions;
    }

    public AuthRequestEvent(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
    }

    public AuthRequestEvent(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken, UUID session) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
        this.session = session;
    }

    public AuthRequestEvent(ClientPermissions permissions, PlayerProfile playerProfile, String accessToken, String protectToken, UUID session, OAuthRequestEvent oauth) {
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

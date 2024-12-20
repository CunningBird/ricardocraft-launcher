package ru.ricardocraft.backend.manangers;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportExtendedCheckServer;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.auth.core.interfaces.user.UserSupportProperties;
import ru.ricardocraft.backend.auth.core.interfaces.user.UserSupportTextures;
import ru.ricardocraft.backend.auth.password.*;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;
import ru.ricardocraft.backend.dto.socket.auth.AuthResponse;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.service.auth.RestoreResponseService;
import ru.ricardocraft.backend.socket.Client;

import javax.crypto.Cipher;
import java.io.IOException;
import java.util.*;

@Component
public class AuthManager {

    private final Logger logger = LogManager.getLogger(AuthManager.class);

    private final KeyAgreementManager keyAgreementManager;
    private final LaunchServerProperties properties;
    private final ProtectHandler protectHandler;
    private final JwtParser checkServerTokenParser;

    @Autowired
    public AuthManager(LaunchServerProperties properties,
                       ProtectHandler protectHandler,
                       KeyAgreementManager keyAgreementManager) {
        this.keyAgreementManager = keyAgreementManager;
        this.properties = properties;
        this.protectHandler = protectHandler;
        this.checkServerTokenParser = Jwts.parser()
                .requireIssuer("LaunchServer")
                .require("tokenType", "checkServer")
                .verifyWith(keyAgreementManager.ecdsaPublicKey)
                .build();
    }

    public String newCheckServerToken(String serverName, String authId, boolean publicOnly) {
        return Jwts.builder()
                .issuer("LaunchServer")
                .claim("serverName", serverName)
                .claim("authId", authId)
                .claim("tokenType", "checkServer")
                .claim("isPublic", publicOnly)
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    public CheckServerTokenInfo parseCheckServerToken(String token) {
        try {
            var jwt = checkServerTokenParser.parseClaimsJws(token).getBody();
            var isPublicClaim = jwt.get("isPublic", Boolean.class);
            return new CheckServerTokenInfo(jwt.get("serverName", String.class), jwt.get("authId", String.class), isPublicClaim == null || isPublicClaim);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Create AuthContext
     *
     * @return AuthContext instance
     */
    public AuthResponseService.AuthContext makeAuthContext(Client client, AuthResponse.ConnectTypes authType, AuthProviderPair pair, String login, String profileName, String ip) {
        Objects.requireNonNull(client, "Client must be not null");
        Objects.requireNonNull(authType, "authType must be not null");
        Objects.requireNonNull(pair, "AuthProviderPair must be not null");
        return new AuthResponseService.AuthContext(client, login, profileName, ip, authType, pair);
    }

    /**
     * Validate auth params ans state
     *
     * @param context Auth context
     * @throws AuthException auth not possible
     */
    public void check(AuthResponseService.AuthContext context) throws AuthException {
        if (context.authType == AuthResponse.ConnectTypes.CLIENT && !context.client.checkSign) {
            throw new AuthException("Don't skip Launcher Update");
        }
        if (context.client.isAuth) {
            throw new AuthException("You are already logged in");
        }
    }

    /**
     * Full client authorization with password verification
     *
     * @param context  AuthContext
     * @param password User password
     * @return Access token
     */
    public AuthReport auth(AuthResponseService.AuthContext context, AuthPassword password) throws AuthException {
        AuthCoreProvider provider = context.pair.core;
        provider.verifyAuth(context);
        if (password instanceof AuthOAuthPassword password1) {
            UserSession session;
            try {
                session = provider.getUserSessionByOAuthAccessToken(password1.accessToken);
            } catch (AuthCoreProvider.OAuthAccessTokenExpired oAuthAccessTokenExpired) {
                throw new AuthException(AuthRequestEvent.OAUTH_TOKEN_EXPIRE);
            }
            if (session == null) {
                throw new AuthException(AuthRequestEvent.OAUTH_TOKEN_INVALID);
            }
            User user = session.getUser();
            context.client.coreObject = user;
            context.client.sessionObject = session;
            internalAuth(context.client, context.authType, context.pair, user.getUsername(), user.getUUID(), user.getPermissions(), true);
            if (context.authType == AuthResponse.ConnectTypes.CLIENT && protectHandler.allowGetAccessToken(context)) {
                return AuthReport.ofMinecraftAccessToken(session.getMinecraftAccessToken(), session);
            }
            return AuthReport.ofMinecraftAccessToken(null, session);
        }
        String login = context.login;
        try {
            AuthReport result = provider.authorize(login, context, password, context.authType == AuthResponse.ConnectTypes.CLIENT && protectHandler.allowGetAccessToken(context));
            if (result == null || result.session == null || result.session.getUser() == null) {
                logger.error("AuthCoreProvider {} method 'authorize' return null", context.pair.name);
                throw new AuthException("Internal Auth Error");
            }
            var session = result.session;
            var user = session.getUser();
            context.client.coreObject = user;
            context.client.sessionObject = session;
            internalAuth(context.client, context.authType, context.pair, user.getUsername(), user.getUUID(), user.getPermissions(), result.isUsingOAuth());
            return result;
        } catch (IOException e) {
            if (e instanceof AuthException authException) throw authException;
            logger.error(e);
            throw new AuthException("Internal Auth Error");
        }
    }

    /**
     * Writing authorization information to the Client object
     */
    public void internalAuth(Client client, AuthResponse.ConnectTypes authType, AuthProviderPair pair, String username, UUID uuid, ClientPermissions permissions, boolean oauth) {
        if (!oauth) {
            throw new UnsupportedOperationException("Unsupported legacy session system");
        }
        client.isAuth = true;
        client.permissions = permissions;
        client.auth_id = pair.name;
        client.auth = pair;
        client.username = username;
        client.type = authType;
        client.uuid = uuid;
    }

    public UserSessionSupportKeys.ClientProfileKeys createClientProfileKeys(UUID playerUUID) {
        throw new UnsupportedOperationException("Minecraft 1.19.1 signature"); // TODO
    }

    public CheckServerReport checkServer(Client client, String username, String serverID) throws IOException {
        if (client.auth == null) return null;
        var supportExtended = client.auth.core.isSupport(AuthSupportExtendedCheckServer.class);
        if(supportExtended != null) {
            var session = supportExtended.extendedCheckServer(client, username, serverID);
            if(session == null) return null;
            return CheckServerReport.ofUserSession(session, getPlayerProfile(client.auth, session.getUser()));
        } else {
            var user = client.auth.core.checkServer(client, username, serverID);
            if (user == null) return null;
            return CheckServerReport.ofUser(user, getPlayerProfile(client.auth, user));
        }
    }

    public boolean joinServer(Client client, String username, UUID uuid, String accessToken, String serverID) throws IOException {
        if (client.auth == null) return false;
        return client.auth.core.joinServer(client, username, uuid, accessToken, serverID);
    }

    public PlayerProfile getPlayerProfile(Client client) {
        if (client.auth == null) return null;
        PlayerProfile playerProfile;
        User user = client.getUser();
        if (user == null) {
            return null;
        }
        playerProfile = getPlayerProfile(client.auth, user);
        if (playerProfile != null) return playerProfile;
        if (client.auth.textureProvider != null) {
            return getPlayerProfile(client.uuid, client.username, client.profile == null ? null : client.profile.getTitle(), client.auth.textureProvider, new HashMap<>());
        }
        // Return combined profile
        return new PlayerProfile(client.uuid, client.username, new HashMap<>(), new HashMap<>());
    }

    public PlayerProfile getPlayerProfile(AuthProviderPair pair, String username) {
        return getPlayerProfile(pair, username, null);
    }

    public PlayerProfile getPlayerProfile(AuthProviderPair pair, String username, ClientProfile profile) {
        UUID uuid;
        User user = pair.core.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        PlayerProfile playerProfile = getPlayerProfile(pair, user);
        uuid = user.getUUID();
        if (playerProfile != null) return playerProfile;
        if (uuid == null) {
            return null;
        }
        if (pair.textureProvider != null) {
            return getPlayerProfile(uuid, username, profile == null ? null : profile.getTitle(), pair.textureProvider, new HashMap<>());
        }
        return new PlayerProfile(uuid, username, new HashMap<>(), new HashMap<>());
    }

    public PlayerProfile getPlayerProfile(AuthProviderPair pair, UUID uuid) {
        return getPlayerProfile(pair, uuid, null);
    }

    public PlayerProfile getPlayerProfile(AuthProviderPair pair, UUID uuid, ClientProfile profile) {
        String username;
        User user = pair.core.getUserByUUID(uuid);
        if (user == null) {
            return null;
        }
        PlayerProfile playerProfile = getPlayerProfile(pair, user);
        username = user.getUsername();
        if (playerProfile != null) return playerProfile;
        if (username == null) {
            return null;
        }
        if (pair.textureProvider != null) {
            return getPlayerProfile(uuid, username, profile == null ? null : profile.getTitle(), pair.textureProvider, new HashMap<>());
        }
        return new PlayerProfile(uuid, username, new HashMap<>(), new HashMap<>());
    }

    public PlayerProfile getPlayerProfile(AuthProviderPair pair, User user) {
        Map<String, String> properties;
        if (user instanceof UserSupportProperties userSupportProperties) {
            properties = userSupportProperties.getProperties();
        } else {
            properties = new HashMap<>();
        }
        if (user instanceof UserSupportTextures userSupportTextures) {
            return new PlayerProfile(user.getUUID(), user.getUsername(), userSupportTextures.getUserAssets(), properties);
        }
        if (pair.textureProvider == null) {
            throw new NullPointerException("TextureProvider not found");
        }
        return getPlayerProfile(user.getUUID(), user.getUsername(), "", pair.textureProvider, properties);
    }

    private PlayerProfile getPlayerProfile(UUID uuid, String username, String client, TextureProvider textureProvider, Map<String, String> properties) {
        // Get skin texture
        var assets = textureProvider.getAssets(uuid, username, client);

        // Return combined profile
        return new PlayerProfile(uuid, username, assets, properties);
    }

    public AuthPassword decryptPassword(AuthPassword password) throws AuthException {
        if (password instanceof Auth2FAPassword auth2FAPassword) {
            auth2FAPassword.firstPassword = tryDecryptPasswordPlain(auth2FAPassword.firstPassword);
            auth2FAPassword.secondPassword = tryDecryptPasswordPlain(auth2FAPassword.secondPassword);
        } else if (password instanceof AuthMultiPassword multiPassword) {
            List<AuthPassword> list = new ArrayList<>(multiPassword.list.size());
            for (AuthPassword p : multiPassword.list) {
                list.add(tryDecryptPasswordPlain(p));
            }
            multiPassword.list = list;
        } else {
            password = tryDecryptPasswordPlain(password);
        }
        return password;
    }

    private AuthPassword tryDecryptPasswordPlain(AuthPassword password) throws AuthException {
        if (password instanceof AuthAESPassword authAESPassword) {
            try {
                return new AuthPlainPassword(IOHelper.decode(SecurityHelper.decrypt(properties.getRuntime().getPasswordEncryptKey(), authAESPassword.password)));
            } catch (Exception ignored) {
                throw new AuthException("Password decryption error");
            }
        }
        if (password instanceof AuthRSAPassword authRSAPassword) {
            try {
                Cipher cipher = SecurityHelper.newRSADecryptCipher(keyAgreementManager.rsaPrivateKey);
                return new AuthPlainPassword(
                        IOHelper.decode(cipher.doFinal(authRSAPassword.password))
                );
            } catch (Exception ignored) {
                throw new AuthException("Password decryption error");
            }
        }
        return password;
    }

    public record CheckServerTokenInfo(String serverName, String authId, boolean isPublic) {
    }

    public static class CheckServerVerifier implements RestoreResponseService.ExtendedTokenProvider {
        private final AuthProviders authProviders;
        private final AuthManager authManager;

        public CheckServerVerifier(AuthManager authManager, AuthProviders authProviders) {
            this.authProviders = authProviders;
            this.authManager = authManager;
        }

        @Override
        public boolean accept(Client client, AuthProviderPair pair, String extendedToken) {
            var info = authManager.parseCheckServerToken(extendedToken);
            if (info == null) {
                return false;
            }
            client.auth_id = info.authId;
            client.auth = authProviders.getAuthProviderPair(info.authId);
            if (client.permissions == null) client.permissions = new ClientPermissions();
            client.permissions.addPerm("launchserver.checkserver");
            if(!info.isPublic) {
                client.permissions.addPerm("launchserver.checkserver.extended");
                client.permissions.addPerm("launchserver.profile.%s.show".formatted(info.serverName));
            }
            client.setProperty("launchserver.serverName", info.serverName);
            return true;
        }
    }

    public static class CheckServerReport {
        public UUID uuid;
        public User user;
        public UserSession session;
        public PlayerProfile playerProfile;

        public CheckServerReport(UUID uuid, User user, UserSession session, PlayerProfile playerProfile) {
            this.uuid = uuid;
            this.user = user;
            this.session = session;
            this.playerProfile = playerProfile;
        }

        public static CheckServerReport ofUser(User user, PlayerProfile playerProfile) {
            return new CheckServerReport(user.getUUID(), user, null, playerProfile);
        }

        public static CheckServerReport ofUserSession(UserSession session, PlayerProfile playerProfile) {
            var user = session.getUser();
            return new CheckServerReport(user.getUUID(), user, session, playerProfile);
        }

        public static CheckServerReport ofUUID(UUID uuid, PlayerProfile playerProfile) {
            return new CheckServerReport(uuid, null, null, playerProfile);
        }
    }

    public record AuthReport(String minecraftAccessToken, String oauthAccessToken,
                             String oauthRefreshToken, long oauthExpire,
                             UserSession session) {

        public static AuthReport ofOAuth(String oauthAccessToken, String oauthRefreshToken, long oauthExpire, UserSession session) {
            return new AuthReport(null, oauthAccessToken, oauthRefreshToken, oauthExpire, session);
        }

        public static AuthReport ofOAuthWithMinecraft(String minecraftAccessToken, String oauthAccessToken, String oauthRefreshToken, long oauthExpire, UserSession session) {
            return new AuthReport(minecraftAccessToken, oauthAccessToken, oauthRefreshToken, oauthExpire, session);
        }

        public static AuthReport ofMinecraftAccessToken(String minecraftAccessToken, UserSession session) {
            return new AuthReport(minecraftAccessToken, null, null, 0, session);
        }

        public boolean isUsingOAuth() {
            return oauthAccessToken != null || oauthRefreshToken != null;
        }
    }
}

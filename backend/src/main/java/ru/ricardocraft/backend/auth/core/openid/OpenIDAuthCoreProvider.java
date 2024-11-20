package ru.ricardocraft.backend.auth.core.openid;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.HikariSQLSourceConfig;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;
import ru.ricardocraft.backend.base.request.auth.password.AuthCodePassword;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OpenIDAuthCoreProvider extends AuthCoreProvider {

    private final Logger logger = LoggerFactory.getLogger(OpenIDAuthCoreProvider.class);

    private transient SQLUserStore sqlUserStore;
    private transient SQLServerSessionStore sqlSessionStore;
    private transient OpenIDAuthenticator openIDAuthenticator;
    private final transient KeyAgreementManager keyAgreementManager;

    private HikariSQLSourceConfig sqlSourceConfig;

    @Autowired
    public OpenIDAuthCoreProvider(KeyAgreementManager keyAgreementManager) {
        this.keyAgreementManager = keyAgreementManager;
        // TODO enablie this
//        this.sqlSourceConfig.init();
//        this.sqlUserStore = new SQLUserStore(sqlSourceConfig);
//        this.sqlUserStore.init();
//        this.sqlSessionStore = new SQLServerSessionStore(sqlSourceConfig);
//        this.sqlSessionStore.init();
//        this.openIDAuthenticator = openIDAuthenticator;
    }

    @Override
    public List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getDetails(Client client) {
        return openIDAuthenticator.getDetails();
    }

    @Override
    public User getUserByUsername(String username) {
        return sqlUserStore.getByUsername(username);
    }

    @Override
    public User getUserByUUID(UUID uuid) {
        return sqlUserStore.getUserByUUID(uuid);
    }

    @Override
    public UserSession getUserSessionByOAuthAccessToken(String accessToken) throws OAuthAccessTokenExpired {
        return openIDAuthenticator.getUserSessionByOAuthAccessToken(accessToken);
    }

    @Override
    public AuthManager.AuthReport refreshAccessToken(String oldRefreshToken, AuthResponseService.AuthContext context) {
        var tokens = openIDAuthenticator.refreshAccessToken(oldRefreshToken);
        var accessToken = tokens.accessToken();
        var refreshToken = tokens.refreshToken();
        long expiresIn = TimeUnit.SECONDS.toMillis(tokens.accessTokenExpiresIn());

        UserSession session;
        try {
            session = openIDAuthenticator.getUserSessionByOAuthAccessToken(accessToken);
        } catch (OAuthAccessTokenExpired e) {
            throw new RuntimeException("invalid token", e);
        }


        return AuthManager.AuthReport.ofOAuth(accessToken, refreshToken,
                expiresIn, session);
    }

    @Override
    public AuthManager.AuthReport authorize(String login, AuthResponseService.AuthContext context, AuthPassword password, boolean minecraftAccess) throws IOException {
        if (password == null) {
            throw AuthException.wrongPassword();
        }
        var authCodePassword = (AuthCodePassword) password;

        var tokens = openIDAuthenticator.authorize(authCodePassword);

        var accessToken = tokens.accessToken();
        var refreshToken = tokens.refreshToken();
        var user = openIDAuthenticator.createUserFromToken(accessToken);
        long expiresIn = TimeUnit.SECONDS.toMillis(tokens.accessTokenExpiresIn());

        sqlUserStore.createOrUpdateUser(user);

        UserSession session;
        try {
            session = openIDAuthenticator.getUserSessionByOAuthAccessToken(accessToken);
        } catch (OAuthAccessTokenExpired e) {
            throw new AuthException("invalid token", e);
        }

        if (minecraftAccess) {
            var minecraftToken = generateMinecraftToken(user);
            return AuthManager.AuthReport.ofOAuthWithMinecraft(minecraftToken, accessToken, refreshToken,
                    expiresIn, session);
        } else {
            return AuthManager.AuthReport.ofOAuth(accessToken, refreshToken,
                    expiresIn, session);
        }
    }

    private String generateMinecraftToken(User user) {
        return Jwts.builder()
                .issuer("LaunchServer")
                .subject(user.getUUID().toString())
                .claim("preferred_username", user.getUsername())
                .expiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    private User createUserFromMinecraftToken(String accessToken) throws AuthException {
        try {
            var parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(keyAgreementManager.ecdsaPublicKey)
                    .build();
            var claims = parser.parseSignedClaims(accessToken);
            var username = claims.getPayload().get("preferred_username", String.class);
            var uuid = UUID.fromString(claims.getPayload().getSubject());
            return new UserEntity(username, uuid, new ClientPermissions());
        } catch (JwtException e) {
            throw new AuthException("Bad minecraft token", e);
        }
    }

    @Override
    public User checkServer(Client client, String username, String serverID) throws IOException {
        var savedServerId = sqlSessionStore.getServerIdByUsername(username);
        if (!serverID.equals(savedServerId)) {
            return null;
        }

        return sqlUserStore.getByUsername(username);
    }

    @Override
    public boolean joinServer(Client client, String username, UUID uuid, String accessToken, String serverID) throws IOException {
        User user;
        try {
            user = createUserFromMinecraftToken(accessToken);
        } catch (AuthException e) {
            logger.error(e.getMessage());
            return false;
        }
        if (!user.getUUID().equals(uuid)) {
            return false;
        }

        sqlUserStore.createOrUpdateUser(user);

        return sqlSessionStore.joinServer(user.getUUID(), user.getUsername(), serverID);
    }

//    @Override
//    public void close() {
//        sqlSourceConfig.close();
//    }

}

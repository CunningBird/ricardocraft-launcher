package ru.ricardocraft.backend.auth.core.openid;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Jwks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.auth.password.AuthCodePassword;
import ru.ricardocraft.backend.auth.password.AuthPassword;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.OpenIDProperties;
import ru.ricardocraft.backend.repository.*;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.QueryBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class OpenIDAuthCoreProvider extends AuthCoreProvider {

    private final Logger logger = LoggerFactory.getLogger(OpenIDAuthCoreProvider.class);

    private final HttpClient CLIENT = HttpClient.newBuilder().build();
    private OpenIDProperties openIdProperties;
    private JwtParser jwtParser;
    private JacksonManager jacksonManager;

    private transient SQLUserStore sqlUserStore;
    private transient SQLServerSessionStore sqlSessionStore;
    private transient KeyAgreementManager keyAgreementManager;

    private HikariSQLSourceConfig sqlSourceConfig;

    @Autowired
    public OpenIDAuthCoreProvider(LaunchServerProperties properties,
                                  JacksonManager jacksonManager,
                                  KeyAgreementManager keyAgreementManager) {

//        this.sqlSourceConfig.init();
//        this.sqlUserStore = new SQLUserStore(sqlSourceConfig);
//        this.sqlUserStore.init();
//        this.sqlSessionStore = new SQLServerSessionStore(sqlSourceConfig);
//        this.sqlSessionStore.init();

//        this.keyAgreementManager = keyAgreementManager;
//        this.openIdProperties = properties.getOpenid();
//        this.jacksonManager = jacksonManager;
//        var keyLocator = loadKeyLocator(openIdProperties);
//        this.jwtParser = Jwts.parser()
//                .keyLocator(keyLocator)
//                .requireIssuer(openIdProperties.getIssuer())
//                .require("azp", openIdProperties.getClientId())
//                .build();
    }

    @Override
    public List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getDetails(Client client) {
        return getDetails();
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
        return getUserSessionByOAuthAccessTokenD(accessToken);
    }

    @Override
    public AuthManager.AuthReport refreshAccessToken(String oldRefreshToken, AuthResponseService.AuthContext context) throws JsonProcessingException {
        var tokens = refreshAccessToken(oldRefreshToken);
        var accessToken = tokens.accessToken();
        var refreshToken = tokens.refreshToken();
        long expiresIn = TimeUnit.SECONDS.toMillis(tokens.accessTokenExpiresIn());

        UserSession session;
        try {
            session = getUserSessionByOAuthAccessTokenD(accessToken);
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

        var tokens = authorize(authCodePassword);

        var accessToken = tokens.accessToken();
        var refreshToken = tokens.refreshToken();
        var user = createUserFromToken(accessToken);
        long expiresIn = TimeUnit.SECONDS.toMillis(tokens.accessTokenExpiresIn());

        sqlUserStore.createOrUpdateUser(user);

        UserSession session;
        try {
            session = getUserSessionByOAuthAccessTokenD(accessToken);
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

    private KeyLocator loadKeyLocator(OpenIDProperties openIdProperties) {
        var request = HttpRequest.newBuilder(openIdProperties.getJwksUri()).GET().build();
        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new KeyLocator(Jwks.setParser().build().parse(response.body()));
    }

    public List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getDetails() {
        var state = UUID.randomUUID().toString();
        var uri = QueryBuilder.get(openIdProperties.getAuthorizationEndpoint())
                .addQuery("response_type", "code")
                .addQuery("client_id", openIdProperties.getClientId())
                .addQuery("redirect_uri", openIdProperties.getRedirectUri())
                .addQuery("scope", openIdProperties.getScopes())
                .addQuery("state", state)
                .toUriString();

        return List.of(new AuthWebViewDetails(uri, openIdProperties.getRedirectUri()));
    }

    public UserSession getUserSessionByOAuthAccessTokenD(String accessToken) throws AuthCoreProvider.OAuthAccessTokenExpired {
        Jws<Claims> token;
        try {
            token = readAndVerifyToken(accessToken);
        } catch (AuthException e) {
            throw new AuthCoreProvider.OAuthAccessTokenExpired("Can't read token", e);
        }
        var user = createUserFromToken(token);
        long expiresIn = 0;
        var expDate = token.getPayload().getExpiration();
        if (expDate != null) {
            expiresIn = expDate.toInstant().toEpochMilli();
        }

        return new OpenIDUserSession(user, accessToken, expiresIn);
    }

    public TokenResponse refreshAccessToken(String oldRefreshToken) throws JsonProcessingException {
        var postBody = QueryBuilder.post()
                .addQuery("grant_type", "refresh_token")
                .addQuery("refresh_token", oldRefreshToken)
                .addQuery("client_id", openIdProperties.getClientId())
                .addQuery("client_secret", openIdProperties.getClientSecret())
                .toString();

        var accessTokenResponse = requestToken(postBody);
        var accessToken = accessTokenResponse.accessToken();
        var refreshToken = accessTokenResponse.refreshToken();

        try {
            readAndVerifyToken(accessToken);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }

        var accessTokenExpiresIn = Objects.requireNonNullElse(accessTokenResponse.expiresIn(), 0L);
        var refreshTokenExpiresIn = Objects.requireNonNullElse(accessTokenResponse.refreshExpiresIn(), 0L);

        return new TokenResponse(accessToken, accessTokenExpiresIn,
                refreshToken, refreshTokenExpiresIn);
    }

    public TokenResponse authorize(AuthCodePassword authCode) throws IOException {
        var uri = URI.create(authCode.uri);
        var queries = splitUriQuery(uri);

        String code = multimapFirstOrNullValue("code", queries);
        String error = multimapFirstOrNullValue("error", queries);
        String errorDescription = multimapFirstOrNullValue("error_description", queries);

        if (error != null && !error.isBlank()) {
            throw new AuthException("Auth error. Error: %s, description: %s".formatted(error, errorDescription));
        }

        var postBody = QueryBuilder.post()
                .addQuery("grant_type", "authorization_code")
                .addQuery("code", code)
                .addQuery("redirect_uri", openIdProperties.getRedirectUri())
                .addQuery("client_id", openIdProperties.getClientId())
                .addQuery("client_secret", openIdProperties.getClientSecret())
                .toString();

        var accessTokenResponse = requestToken(postBody);
        var accessToken = accessTokenResponse.accessToken();
        var refreshToken = accessTokenResponse.refreshToken();

        readAndVerifyToken(accessToken);

        var accessTokenExpiresIn = Objects.requireNonNullElse(accessTokenResponse.expiresIn(), 0L);
        var refreshTokenExpiresIn = Objects.requireNonNullElse(accessTokenResponse.refreshExpiresIn(), 0L);

        return new TokenResponse(accessToken, accessTokenExpiresIn,
                refreshToken, refreshTokenExpiresIn);
    }

    public User createUserFromToken(String accessToken) throws AuthException {
        return createUserFromToken(readAndVerifyToken(accessToken));
    }

    private Jws<Claims> readAndVerifyToken(String accessToken) throws AuthException {
        if (accessToken == null) {
            throw new AuthException("Token is null");
        }

        try {
            return jwtParser.parseSignedClaims(accessToken);
        } catch (JwtException e) {
            throw new AuthException("Bad token", e);
        }
    }

    private User createUserFromToken(Jws<Claims> token) {
        var username = token.getPayload().get(openIdProperties.getExtractor().getUsernameClaim(), String.class);
        var uuidStr = token.getPayload().get(openIdProperties.getExtractor().getUuidClaim(), String.class);
        var uuid = UUID.fromString(uuidStr);
        return new UserEntity(username, uuid, new ClientPermissions());
    }

    private AccessTokenResponse requestToken(String postBody) throws JsonProcessingException {
        var request = HttpRequest.newBuilder()
                .uri(openIdProperties.getTokenUri())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(postBody))
                .build();

        HttpResponse<String> resp;
        try {
            resp = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return jacksonManager.getMapper().readValue(resp.body(), AccessTokenResponse.class);
    }
}

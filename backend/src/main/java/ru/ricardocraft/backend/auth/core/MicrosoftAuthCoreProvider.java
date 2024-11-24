package ru.ricardocraft.backend.auth.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.auth.password.AuthCodePassword;
import ru.ricardocraft.backend.auth.password.AuthPassword;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.MicrosoftAuthCoreProviderProperties;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.HttpRequester;
import ru.ricardocraft.backend.socket.handlers.error.BasicJsonHttpErrorHandler;
import ru.ricardocraft.backend.socket.handlers.error.MicrosoftErrorHandler;
import ru.ricardocraft.backend.socket.handlers.error.XSTSErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class MicrosoftAuthCoreProvider extends MojangAuthCoreProvider {

    private transient final Logger logger = LogManager.getLogger(MicrosoftAuthCoreProvider.class);

    private transient final MicrosoftAuthCoreProviderProperties properties;
    private transient final HttpRequester requester;

    @Autowired
    public MicrosoftAuthCoreProvider(JacksonManager jacksonManager,
                                     LaunchServerProperties properties,
                                     HttpRequester requester) {
        super(jacksonManager);
        this.properties = properties.getMicrosoftAuthCoreProvider();
        this.requester = requester;
    }

    @Override
    public List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getDetails(Client client) {
        String uuid = UUID.randomUUID().toString();
        client.setStaticProperty("microsoftCode", uuid);
        return List.of(new AuthWebViewDetails(
                properties.getAuthCodeUrl().formatted(
                        properties.getClientId(),
                        properties.getRedirectUrl().formatted(uuid)
                ), properties.getRedirectUrl().formatted(uuid)
        ));
    }

    @Override
    public UserSession getUserSessionByOAuthAccessToken(String accessToken) throws OAuthAccessTokenExpired {
        return super.getUserSessionByOAuthAccessToken(accessToken);
    }

    @Override
    public AuthManager.AuthReport refreshAccessToken(String refreshToken, AuthResponseService.AuthContext context) {
        try {
            var result = sendMicrosoftOAuthRefreshTokenRequest(refreshToken);
            if (result == null) {
                return null;
            }
            var response = getMinecraftTokenByMicrosoftToken(result.access_token);
            return AuthManager.AuthReport.ofOAuth(response.access_token, result.refresh_token, SECONDS.toMillis(response.expires_in), null);
        } catch (IOException e) {
            logger.error("Microsoft refresh failed", e);
            return null;
        }
    }

    @Override
    public AuthManager.AuthReport authorize(String login, AuthResponseService.AuthContext context, AuthPassword password, boolean minecraftAccess) throws IOException {
        if (password == null) {
            throw AuthException.wrongPassword();
        }
        AuthCodePassword codePassword = (AuthCodePassword) password;
        var uri = URI.create(codePassword.uri);
        var queries = splitUriQuery(uri);
        var code = multimapFirstOrNullValue("code", queries);
        try {
            var token = sendMicrosoftOAuthTokenRequest(code);
            if (token == null) {
                throw new AuthException("Microsoft auth error: oauth token");
            }
            try {
                var response = getMinecraftTokenByMicrosoftToken(token.access_token);
                var session = getUserSessionByOAuthAccessToken(response.access_token);
                if (minecraftAccess) {
                    return AuthManager.AuthReport.ofOAuthWithMinecraft(response.access_token, response.access_token, token.refresh_token, SECONDS.toMillis(response.expires_in), session);
                } else {
                    return AuthManager.AuthReport.ofOAuth(response.access_token, token.refresh_token, SECONDS.toMillis(response.expires_in), session);
                }
            } catch (OAuthAccessTokenExpired e) {
                throw new AuthException("Internal Auth Error: Token invalid");
            }
        } catch (RequestException e) {
            throw new AuthException(e.toString());
        }
    }

    private MinecraftLoginWithXBoxResponse getMinecraftTokenByMicrosoftToken(String microsoftAccessToken) throws IOException {
        // XBox Live
        var xboxLive = sendMicrosoftXBoxLiveRequest(microsoftAccessToken);
        // XSTS
        var xsts = sendMicrosoftXSTSRequest(xboxLive.Token);
        // Minecraft auth
        return sendMinecraftLoginWithXBoxRequest(xsts.getUHS(), xsts.Token);
    }

    private URI makeOAuthTokenRequestURI(String code) throws IOException {
        URI uri;
        try {
            if (properties.getClientSecret() != null) {
                uri = new URI("https://login.live.com/oauth20_token.srf?client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s".formatted(
                        properties.getClientSecret(), properties.getClientSecret(), code, URLEncoder.encode(properties.getRedirectUrl(), StandardCharsets.UTF_8)));
            } else {
                uri = new URI("https://login.live.com/oauth20_token.srf?client_id=%s&code=%s&grant_type=authorization_code&redirect_uri=%s".formatted(
                        properties.getClientId(), code, URLEncoder.encode(properties.getRedirectUrl(), StandardCharsets.UTF_8)));
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return uri;
    }

    private URI makeOAuthRefreshTokenRequestURI(String refreshToken) throws IOException {
        URI uri;
        try {
            if (properties.getClientSecret() != null) {
                uri = new URI("https://login.live.com/oauth20_token.srf?client_id=%s&client_secret=%s&refresh_token=%s&grant_type=refresh_token".formatted(
                        properties.getClientId(), properties.getClientSecret(), refreshToken));
            } else {
                uri = new URI("https://login.live.com/oauth20_token.srf?client_id=%s&refresh_token=%s&grant_type=refresh_token".formatted(
                        properties.getClientId(), refreshToken));
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        return uri;
    }

    private MicrosoftOAuthTokenResponse sendMicrosoftOAuthTokenRequest(String code) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeOAuthTokenRequestURI(code))
                .GET()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        var e = requester.send(request, new MicrosoftErrorHandler<>(MicrosoftOAuthTokenResponse.class));
        return e.getOrThrow();
    }

    private MicrosoftOAuthTokenResponse sendMicrosoftOAuthRefreshTokenRequest(String refreshToken) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeOAuthRefreshTokenRequestURI(refreshToken))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        var e = requester.send(request, new MicrosoftErrorHandler<>(MicrosoftOAuthTokenResponse.class));
        return e.getOrThrow();
    }

    private MicrosoftXBoxLiveResponse sendMicrosoftXBoxLiveRequest(String accessToken) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI("https://user.auth.xboxlive.com/user/authenticate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(jsonBodyPublisher(new MicrosoftXBoxLiveRequest(accessToken)))
                .build();
        var e = requester.send(request, new BasicJsonHttpErrorHandler<>(MicrosoftXBoxLiveResponse.class));
        return e.getOrThrow();
    }

    private MicrosoftXBoxLiveResponse sendMicrosoftXSTSRequest(String xblToken) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI("https://xsts.auth.xboxlive.com/xsts/authorize"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(jsonBodyPublisher(new MicrosoftXSTSRequest(xblToken)))
                .build();
        var e = requester.send(request, new XSTSErrorHandler<>(MicrosoftXBoxLiveResponse.class));
        return e.getOrThrow();
    }

    private MinecraftLoginWithXBoxResponse sendMinecraftLoginWithXBoxRequest(String uhs, String xstsToken) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(makeURI("https://api.minecraftservices.com/authentication/login_with_xbox"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(jsonBodyPublisher(new MinecraftLoginWithXBoxRequest(uhs, xstsToken)))
                .build();
        var e = requester.send(request, new BasicJsonHttpErrorHandler<>(MinecraftLoginWithXBoxResponse.class));
        return e.getOrThrow();
    }

    public <T> HttpRequest.BodyPublisher jsonBodyPublisher(T obj) throws JsonProcessingException {
        return HttpRequest.BodyPublishers.ofString(jacksonManager.getMapper().writeValueAsString(obj));
    }

    private URI makeURI(String s) throws IOException {
        try {
            return new URI(s);
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    public record XSTSError(String Identity, long XErr, String Message, String Redirect) {
        @Override
        public String toString() {
            if (Message != null && !Message.isEmpty()) {
                return Message;
            }
            if (XErr == 2148916233L) {
                return "The account doesn't have an Xbox account.";
            }
            if (XErr == 2148916235L) {
                return "The account is from a country where Xbox Live is not available/banned";
            }
            if (XErr == 2148916238L) {
                return "The account is a child (under 18) and cannot proceed unless the account is added to a Family by an adult";
            }
            return "XSTS error: %d".formatted(XErr);
        }
    }

    public record MicrosoftError(String error, String error_description, String correlation_id) {
        @Override
        public String toString() {
            return error_description;
        }
    }

    public record MicrosoftOAuthTokenResponse(String token_type, long expires_in, String scope, String access_token,
                                              String refresh_token, String user_id, String foci) {
    }

    public record MicrosoftXBoxLivePropertiesRequest(String AuthMethod, String SiteName, String RpsTicket) {
        public MicrosoftXBoxLivePropertiesRequest(String accessToken) {
            this("RPS", "user.auth.xboxlive.com", "d=".concat(accessToken));
        }
    }

    public record MicrosoftXBoxLiveRequest(MicrosoftXBoxLivePropertiesRequest Properties, String RelyingParty,
                                           String TokenType) {
        public MicrosoftXBoxLiveRequest(String accessToken) {
            this(new MicrosoftXBoxLivePropertiesRequest(accessToken), "http://auth.xboxlive.com", "JWT");
        }
    }

    public record MicrosoftXBoxLiveResponse(String IssueInstant, String NotAfter, String Token,
                                            Map<String, List<Map<String, String>>> DisplayClaims) { //XBox Live and XSTS
        public String getUHS() {
            return DisplayClaims.get("xui").getFirst().get("uhs");
        }
    }

    public record MicrosoftXSTSPropertiesRequest(String SandboxId, List<String> UserTokens) {
        public MicrosoftXSTSPropertiesRequest(String xblToken) {
            this("RETAIL", List.of(xblToken));
        }
    }

    public record MicrosoftXSTSRequest(MicrosoftXSTSPropertiesRequest Properties, String RelyingParty,
                                       String TokenType) {
        public MicrosoftXSTSRequest(String xblToken) {
            this(new MicrosoftXSTSPropertiesRequest(xblToken), "rp://api.minecraftservices.com/", "JWT");
        }
    }

    public record MinecraftLoginWithXBoxRequest(String identityToken) {
        public MinecraftLoginWithXBoxRequest(String uhs, String xstsToken) {
            this("XBL3.0 x=%s;%s".formatted(uhs, xstsToken));
        }
    }

    public record MinecraftLoginWithXBoxResponse(String username, List<String> roles, String access_token,
                                                 String token_type, long expires_in) {
    }
}

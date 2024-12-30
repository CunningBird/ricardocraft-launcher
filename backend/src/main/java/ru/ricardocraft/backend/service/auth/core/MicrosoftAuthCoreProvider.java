package ru.ricardocraft.backend.service.auth.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.dto.request.RequestException;
import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.MicrosoftAuthCoreProviderProperties;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.service.auth.password.AuthCodePassword;
import ru.ricardocraft.backend.service.auth.password.AuthPassword;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
public class MicrosoftAuthCoreProvider extends MojangAuthCoreProvider {

    private final MicrosoftAuthCoreProviderProperties properties;
    private final RestClient restClient;

    @Autowired
    public MicrosoftAuthCoreProvider(ObjectMapper objectMapper, LaunchServerProperties properties, RestClient restClient) {
        super(objectMapper);
        this.properties = properties.getMicrosoftAuthCoreProvider();
        this.restClient = restClient;
    }

    @Override
    public List<GetAvailabilityAuthResponse.AuthAvailabilityDetails> getDetails(Client client) {
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
    public AuthService.AuthReport refreshAccessToken(String refreshToken, AuthController.AuthContext context) {
        try {
            var result = sendMicrosoftOAuthRefreshTokenRequest(refreshToken);
            if (result == null) {
                return null;
            }
            var response = getMinecraftTokenByMicrosoftToken(result.access_token);
            return AuthService.AuthReport.ofOAuth(response.access_token, result.refresh_token, SECONDS.toMillis(response.expires_in), null);
        } catch (IOException e) {
            log.error("Microsoft refresh failed", e);
            return null;
        }
    }

    @Override
    public AuthService.AuthReport authorize(String login, AuthController.AuthContext context, AuthPassword password, boolean minecraftAccess) throws IOException {
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
                    return AuthService.AuthReport.ofOAuthWithMinecraft(response.access_token, response.access_token, token.refresh_token, SECONDS.toMillis(response.expires_in), session);
                } else {
                    return AuthService.AuthReport.ofOAuth(response.access_token, token.refresh_token, SECONDS.toMillis(response.expires_in), session);
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
        return restClient.get()
                .uri(makeOAuthTokenRequestURI(code))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .onStatus(status -> status.value() < 200 || status.value() >= 300, (request, response) -> {
                    MicrosoftAuthCoreProvider.XSTSError errorText = objectMapper.readValue(response.getBody(), MicrosoftAuthCoreProvider.XSTSError.class);
                    throw new IOException(errorText.toString());
                })
                .body(MicrosoftOAuthTokenResponse.class);
    }

    private MicrosoftOAuthTokenResponse sendMicrosoftOAuthRefreshTokenRequest(String refreshToken) throws IOException {
        return restClient.post()
                .uri(makeOAuthRefreshTokenRequestURI(refreshToken))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .onStatus(status -> status.value() < 200 || status.value() >= 300, (request, response) -> {
                    MicrosoftAuthCoreProvider.XSTSError errorText = objectMapper.readValue(response.getBody(), MicrosoftAuthCoreProvider.XSTSError.class);
                    throw new IOException(errorText.toString());
                })
                .body(MicrosoftOAuthTokenResponse.class);
    }

    private MicrosoftXBoxLiveResponse sendMicrosoftXBoxLiveRequest(String accessToken) throws IOException {
        return restClient.post()
                .uri(makeURI("https://user.auth.xboxlive.com/user/authenticate"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(new MicrosoftXBoxLiveRequest(accessToken))
                .retrieve()
                .body(MicrosoftXBoxLiveResponse.class);
    }

    private MicrosoftXBoxLiveResponse sendMicrosoftXSTSRequest(String xblToken) throws IOException {
        return restClient.post()
                .uri(makeURI("https://xsts.auth.xboxlive.com/xsts/authorize"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(new MicrosoftXSTSRequest(xblToken))
                .retrieve()
                .body(MicrosoftXBoxLiveResponse.class);
    }

    private MinecraftLoginWithXBoxResponse sendMinecraftLoginWithXBoxRequest(String uhs, String xstsToken) throws IOException {
        return restClient.post()
                .uri(makeURI("https://api.minecraftservices.com/authentication/login_with_xbox"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(new MinecraftLoginWithXBoxRequest(uhs, xstsToken))
                .retrieve()
                .body(MinecraftLoginWithXBoxResponse.class);
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

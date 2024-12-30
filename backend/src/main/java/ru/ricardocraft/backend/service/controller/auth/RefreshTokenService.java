package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.RefreshTokenRequest;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.RefreshTokenResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;

@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private final AuthProviders authProviders;

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, Client client) throws Exception {
        if (request.refreshToken == null) {
            throw new Exception("Invalid request");
        }
        AuthProviderPair pair;
        if (!client.isAuth) {
            if (request.authId == null) {
                pair = authProviders.getAuthProviderPair();
            } else {
                pair = authProviders.getAuthProviderPair(request.authId);
            }
        } else {
            pair = client.auth;
        }
        if (pair == null) {
            throw new Exception("Invalid request");
        }
        AuthService.AuthReport report = pair.core.refreshAccessToken(request.refreshToken, new AuthRequestService.AuthContext(client, null, null, request.ip, AuthRequest.ConnectTypes.API, pair));
        if (report == null || !report.isUsingOAuth()) {
            throw new Exception("Invalid RefreshToken");
        }
        return new RefreshTokenResponse(new AuthResponse.OAuthRequestEvent(report.oauthAccessToken(), report.oauthRefreshToken(), report.oauthExpire()));
    }
}

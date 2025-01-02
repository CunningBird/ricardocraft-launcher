package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.response.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.client.dto.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.client.dto.request.auth.details.AuthTotpDetails;
import ru.ricardocraft.client.dto.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.client.client.WebSocketRequest;
import ru.ricardocraft.client.base.utils.ProviderMap;

public class GetAvailabilityAuthRequest extends Request<GetAvailabilityAuthRequestEvent> implements WebSocketRequest {

    public static final ProviderMap<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> providers = new ProviderMap<>();
    private static boolean registeredProviders = false;

    public static void registerProviders() {
        if (!registeredProviders) {
            providers.register("password", AuthPasswordDetails.class);
            providers.register("webview", AuthWebViewDetails.class);
            providers.register("totp", AuthTotpDetails.class);
            providers.register("loginonly", AuthLoginOnlyDetails.class);
            registeredProviders = true;
        }
    }

    @Override
    public String getType() {
        return "getAvailabilityAuth";
    }
}

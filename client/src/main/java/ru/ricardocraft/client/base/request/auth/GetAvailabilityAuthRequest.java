package ru.ricardocraft.client.base.request.auth;

import ru.ricardocraft.client.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.client.base.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.client.base.request.auth.details.AuthTotpDetails;
import ru.ricardocraft.client.base.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.client.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.client.utils.ProviderMap;

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

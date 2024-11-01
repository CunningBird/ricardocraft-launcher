package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.GetAvailabilityAuthRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.auth.details.AuthLoginOnlyDetails;
import pro.gravit.launchserver.base.request.auth.details.AuthPasswordDetails;
import pro.gravit.launchserver.base.request.auth.details.AuthTotpDetails;
import pro.gravit.launchserver.base.request.auth.details.AuthWebViewDetails;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;
import pro.gravit.launchserver.utils.ProviderMap;

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

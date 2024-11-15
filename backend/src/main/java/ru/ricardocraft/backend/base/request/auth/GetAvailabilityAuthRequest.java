package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthTotpDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.utils.ProviderMap;

public class GetAvailabilityAuthRequest extends Request<GetAvailabilityAuthRequestEvent> implements WebSocketRequest {

    @Override
    public String getType() {
        return "getAvailabilityAuth";
    }
}

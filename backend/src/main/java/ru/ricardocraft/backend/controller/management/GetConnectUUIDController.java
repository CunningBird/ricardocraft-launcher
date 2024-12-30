package ru.ricardocraft.backend.controller.management;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.service.controller.management.GetConnectUuidService;

@Component
public class GetConnectUUIDController extends AbstractController {

    private final GetConnectUuidService getConnectUuidService;

    public GetConnectUUIDController(ServerWebSocketHandler handler, GetConnectUuidService getConnectUuidService) {
        super(GetConnectUUIDRequest.class, handler);
        this.getConnectUuidService = getConnectUuidService;
    }

    @Override
    public GetConnectUUIDResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return getConnectUuidService.getConnectUuid(castRequest(request));
    }
}

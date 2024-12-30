package ru.ricardocraft.backend.controller.secure;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.service.controller.secure.GetSecureLevelInfoService;

@Component
public class GetSecureLevelInfoController extends AbstractController {

    private final GetSecureLevelInfoService getSecureLevelInfoService;

    public GetSecureLevelInfoController(ServerWebSocketHandler handler, GetSecureLevelInfoService getSecureLevelInfoService) {
        super(GetSecureLevelInfoRequest.class, handler);
        this.getSecureLevelInfoService = getSecureLevelInfoService;
    }

    @Override
    public GetSecureLevelInfoResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return getSecureLevelInfoService.getSecureLevelInfoResponse(client);
    }
}

package ru.ricardocraft.backend.controller.cabinet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.cabinet.GetAssetUploadInfoRequest;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadUrlResponse;
import ru.ricardocraft.backend.service.controller.cabinet.GetAssetUploadInfoService;

@Component
public class GetAssetUploadInfoController extends AbstractController {

    private final GetAssetUploadInfoService getAssetUploadInfoService;

    public GetAssetUploadInfoController(ServerWebSocketHandler handler, GetAssetUploadInfoService getAssetUploadInfoService) {
        super(GetAssetUploadInfoRequest.class, handler);
        this.getAssetUploadInfoService = getAssetUploadInfoService;
    }

    @Override
    public GetAssetUploadUrlResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return getAssetUploadInfoService.getAssetUploadUrl(castRequest(request), client);
    }
}

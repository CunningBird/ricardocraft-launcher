package ru.ricardocraft.backend.controller.cabinet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.cabinet.AssetUploadInfoRequest;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.service.controller.cabinet.AssetUploadInfoService;

@Component
public class AssetUploadInfoController extends AbstractController {

    private final AssetUploadInfoService assetUploadInfoService;

    public AssetUploadInfoController(ServerWebSocketHandler handler, AssetUploadInfoService assetUploadInfoService) {
        super(AssetUploadInfoRequest.class, handler);
        this.assetUploadInfoService = assetUploadInfoService;
    }

    @Override
    public AssetUploadInfoResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return assetUploadInfoService.assetUploadInfo(client);
    }
}

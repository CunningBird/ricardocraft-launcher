package ru.ricardocraft.backend.controller.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadUrlResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.cabinet.GetAssetUploadInfoRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class GetAssetUploadInfoController extends AbstractController {

    @Autowired
    public GetAssetUploadInfoController(ServerWebSocketHandler handler) {
        super(GetAssetUploadInfoRequest.class, handler);
    }

    @Override
    public GetAssetUploadUrlResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        GetAssetUploadInfoRequest response = (GetAssetUploadInfoRequest) rawResponse;

        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            throw new Exception("Access denied");
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            throw new Exception("Not supported");
        }
        return new GetAssetUploadUrlResponse(support.getAssetUploadUrl(response.name, client.getUser()), support.getAssetUploadToken(response.name, client.getUser()));
    }
}

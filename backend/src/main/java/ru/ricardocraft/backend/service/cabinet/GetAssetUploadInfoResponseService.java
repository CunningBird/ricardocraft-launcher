package ru.ricardocraft.backend.service.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.events.request.cabinet.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadInfoResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class GetAssetUploadInfoResponseService extends AbstractResponseService {

    @Autowired
    public GetAssetUploadInfoResponseService(ServerWebSocketHandler handler) {
        super(GetAssetUploadInfoResponse.class, handler);
    }

    @Override
    public GetAssetUploadUrlRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        GetAssetUploadInfoResponse response = (GetAssetUploadInfoResponse) rawResponse;

        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            throw new Exception("Access denied");
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            throw new Exception("Not supported");
        }
        return new GetAssetUploadUrlRequestEvent(support.getAssetUploadUrl(response.name, client.getUser()), support.getAssetUploadToken(response.name, client.getUser()));
    }
}

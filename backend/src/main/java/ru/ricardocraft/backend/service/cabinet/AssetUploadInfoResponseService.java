package ru.ricardocraft.backend.service.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.events.request.cabinet.AssetUploadInfoRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class AssetUploadInfoResponseService extends AbstractResponseService {

    @Autowired
    public AssetUploadInfoResponseService(ServerWebSocketHandler handler) {
        super(AssetUploadInfoResponse.class, handler);
    }

    @Override
    public AssetUploadInfoRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            throw new Exception("Access denied");
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            throw new Exception("Not supported");
        }
        return support.getAssetUploadInfo(client.getUser());
    }
}

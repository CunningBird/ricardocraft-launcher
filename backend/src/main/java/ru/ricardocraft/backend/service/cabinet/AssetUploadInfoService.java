package ru.ricardocraft.backend.service.cabinet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.cabinet.AssetUploadInfoRequest;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class AssetUploadInfoService extends AbstractService {

    @Autowired
    public AssetUploadInfoService(ServerWebSocketHandler handler) {
        super(AssetUploadInfoRequest.class, handler);
    }

    @Override
    public AssetUploadInfoResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
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

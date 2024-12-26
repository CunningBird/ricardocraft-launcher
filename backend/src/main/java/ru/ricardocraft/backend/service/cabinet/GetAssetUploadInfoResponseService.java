package ru.ricardocraft.backend.service.cabinet;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.events.request.cabinet.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadInfoResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class GetAssetUploadInfoResponseService extends AbstractResponseService {

    @Autowired
    public GetAssetUploadInfoResponseService(WebSocketService service) {
        super(GetAssetUploadInfoResponse.class, service);
    }

    @Override
    public GetAssetUploadUrlRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
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

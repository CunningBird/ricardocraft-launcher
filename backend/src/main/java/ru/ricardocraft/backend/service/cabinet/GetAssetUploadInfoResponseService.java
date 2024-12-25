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
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        GetAssetUploadInfoResponse response = (GetAssetUploadInfoResponse) rawResponse;

        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            sendError(ctx, "Access denied", response.requestUUID);
            return;
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            sendError(ctx, "Not supported", response.requestUUID);
            return;
        }
        sendResult(ctx, new GetAssetUploadUrlRequestEvent(support.getAssetUploadUrl(response.name, client.getUser()), support.getAssetUploadToken(response.name, client.getUser())), response.requestUUID);
    }
}

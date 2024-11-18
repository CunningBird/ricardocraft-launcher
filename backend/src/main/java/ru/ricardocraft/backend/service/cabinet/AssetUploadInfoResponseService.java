package ru.ricardocraft.backend.service.cabinet;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class AssetUploadInfoResponseService extends AbstractResponseService {

    @Autowired
    public AssetUploadInfoResponseService(WebSocketService service) {
        super(AssetUploadInfoResponse.class, service);
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        AssetUploadInfoResponse response = (AssetUploadInfoResponse) rawResponse;

        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            sendError(ctx, "Access denied", response.requestUUID);
            return;
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            sendError(ctx, "Not supported", response.requestUUID);
            return;
        }
        sendResult(ctx, support.getAssetUploadInfo(client.getUser()), response.requestUUID);
    }
}

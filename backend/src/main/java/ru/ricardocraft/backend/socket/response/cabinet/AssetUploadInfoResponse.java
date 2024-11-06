package ru.ricardocraft.backend.socket.response.cabinet;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class AssetUploadInfoResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(!client.isAuth || client.auth == null || client.getUser() == null) {
            sendError("Access denied");
            return;
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if(support == null) {
            sendError("Not supported");
            return;
        }
        sendResult(support.getAssetUploadInfo(client.getUser()));
    }
}

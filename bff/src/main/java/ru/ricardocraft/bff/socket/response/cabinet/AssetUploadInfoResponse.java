package ru.ricardocraft.bff.socket.response.cabinet;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

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

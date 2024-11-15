package ru.ricardocraft.backend.socket.response.cabinet;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.base.events.request.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetAssetUploadInfoResponse extends SimpleResponse {
    public String name;
    @Override
    public String getType() {
        return "getAssetUploadUrl";
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
        sendResult(new GetAssetUploadUrlRequestEvent(support.getAssetUploadUrl(name, client.getUser()), support.getAssetUploadToken(name, client.getUser())));
    }
}

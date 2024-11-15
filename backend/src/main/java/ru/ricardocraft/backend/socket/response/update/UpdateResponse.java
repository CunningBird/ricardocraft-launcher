package ru.ricardocraft.backend.socket.response.update;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.events.request.UpdateRequestEvent;
import ru.ricardocraft.backend.core.hasher.HashedDir;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class UpdateResponse extends SimpleResponse {
    public String dirName;

    @Override
    public String getType() {
        return "update";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (config.protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetUpdates(dirName, client)) {
            sendError("Access denied");
            return;
        }
        if (dirName == null) {
            sendError("Invalid request");
            return;
        }
        HashedDir dir = updatesManager.getUpdate(dirName);
        if (dir == null) {
            sendError("Directory %s not found".formatted(dirName));
            return;
        }
        String url = config.netty.downloadURL.replace("%dirname%", IOHelper.urlEncode(dirName));
        boolean zip = false;
        if (config.netty.bindings.get(dirName) != null) {
            LaunchServerConfig.NettyUpdatesBind bind = config.netty.bindings.get(dirName);
            url = bind.url;
            zip = bind.zip;
        }
        sendResult(new UpdateRequestEvent(dir, url, zip));
    }
}

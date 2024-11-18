package ru.ricardocraft.backend.service.update;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.events.request.UpdateRequestEvent;
import ru.ricardocraft.backend.base.core.hasher.HashedDir;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.update.UpdateResponse;

@Component
public class UpdateResponseService extends AbstractResponseService {

    private final LaunchServerConfig config;
    private final ProtectHandler protectHandler;
    private final UpdatesManager updatesManager;

    @Autowired
    public UpdateResponseService(WebSocketService service,
                                 LaunchServerConfig config,
                                 ProtectHandler protectHandler,
                                 UpdatesManager updatesManager) {
        super(UpdateResponse.class, service);
        this.config = config;
        this.protectHandler = protectHandler;
        this.updatesManager = updatesManager;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        UpdateResponse response = (UpdateResponse) rawResponse;

        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetUpdates(response.dirName, client)) {
            sendError(ctx, "Access denied", response.requestUUID);
            return;
        }
        if (response.dirName == null) {
            sendError(ctx, "Invalid request", response.requestUUID);
            return;
        }
        HashedDir dir = updatesManager.getUpdate(response.dirName);
        if (dir == null) {
            sendError(ctx, "Directory %s not found".formatted(response.dirName), response.requestUUID);
            return;
        }
        String url = config.netty.downloadURL.replace("%dirname%", IOHelper.urlEncode(response.dirName));
        boolean zip = false;
        if (config.netty.bindings.get(response.dirName) != null) {
            LaunchServerConfig.NettyUpdatesBind bind = config.netty.bindings.get(response.dirName);
            url = bind.url;
            zip = bind.zip;
        }
        sendResult(ctx, new UpdateRequestEvent(dir, url, zip), response.requestUUID);
    }
}

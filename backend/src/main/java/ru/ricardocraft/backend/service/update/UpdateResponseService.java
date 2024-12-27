package ru.ricardocraft.backend.service.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.hasher.HashedDir;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.dto.events.request.update.UpdateRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerUpdatesBindProperties;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class UpdateResponseService extends AbstractResponseService {

    private final HttpServerProperties httpServerProperties;
    private final ProtectHandler protectHandler;
    private final UpdatesManager updatesManager;

    @Autowired
    public UpdateResponseService(ServerWebSocketHandler handler,
                                 HttpServerProperties httpServerProperties,
                                 ProtectHandler protectHandler,
                                 UpdatesManager updatesManager) {
        super(UpdateResponse.class, handler);
        this.httpServerProperties = httpServerProperties;
        this.protectHandler = protectHandler;
        this.updatesManager = updatesManager;
    }

    @Override
    public UpdateRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        UpdateResponse response = (UpdateResponse) rawResponse;

        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetUpdates(response.dirName, client)) {
            throw new Exception("Access denied");
        }
        if (response.dirName == null) {
            throw new Exception("Invalid request");
        }
        HashedDir dir = updatesManager.getUpdate(response.dirName);
        if (dir == null) {
            throw new Exception("Directory %s not found".formatted(response.dirName));
        }
        String url = httpServerProperties.getDownloadURL().replace("%dirname%", IOHelper.urlEncode(response.dirName));
        boolean zip = false;
        if (httpServerProperties.getBindings().get(response.dirName) != null) {
            HttpServerUpdatesBindProperties bind = httpServerProperties.getBindings().get(response.dirName);
            url = bind.getUrl();
            zip = bind.getZip();
        }
        return new UpdateRequestEvent(dir, url, zip);
    }
}

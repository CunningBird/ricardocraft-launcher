package ru.ricardocraft.backend.controller.update;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.hasher.HashedDir;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.update.UpdateRequest;
import ru.ricardocraft.backend.service.UpdatesService;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerUpdatesBindProperties;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class UpdateController extends AbstractController {

    private final HttpServerProperties httpServerProperties;
    private final ProtectHandler protectHandler;
    private final UpdatesService updatesService;

    @Autowired
    public UpdateController(ServerWebSocketHandler handler,
                            HttpServerProperties httpServerProperties,
                            ProtectHandler protectHandler,
                            UpdatesService updatesService) {
        super(UpdateRequest.class, handler);
        this.httpServerProperties = httpServerProperties;
        this.protectHandler = protectHandler;
        this.updatesService = updatesService;
    }

    @Override
    public UpdateResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        UpdateRequest response = (UpdateRequest) rawResponse;

        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetUpdates(response.dirName, client)) {
            throw new Exception("Access denied");
        }
        if (response.dirName == null) {
            throw new Exception("Invalid request");
        }
        HashedDir dir = updatesService.getUpdate(response.dirName);
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
        return new UpdateResponse(dir, url, zip);
    }
}

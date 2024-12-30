package ru.ricardocraft.backend.service.controller.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.hasher.HashedDir;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.update.UpdateRequest;
import ru.ricardocraft.backend.dto.response.update.UpdateResponse;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerUpdatesBindProperties;
import ru.ricardocraft.backend.service.UpdatesService;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;

@Component
@RequiredArgsConstructor
public class UpdateRequestController {

    private final HttpServerProperties httpServerProperties;
    private final ProtectHandler protectHandler;
    private final UpdatesService updatesService;

    public UpdateResponse update(UpdateRequest request, Client client) throws Exception {
        if (protectHandler instanceof ProfilesProtectHandler profilesProtectHandler && !profilesProtectHandler.canGetUpdates(request.dirName, client)) {
            throw new Exception("Access denied");
        }
        if (request.dirName == null) {
            throw new Exception("Invalid request");
        }
        HashedDir dir = updatesService.getUpdate(request.dirName);
        if (dir == null) {
            throw new Exception("Directory %s not found".formatted(request.dirName));
        }
        String url = httpServerProperties.getDownloadURL().replace("%dirname%", IOHelper.urlEncode(request.dirName));
        boolean zip = false;
        if (httpServerProperties.getBindings().get(request.dirName) != null) {
            HttpServerUpdatesBindProperties bind = httpServerProperties.getBindings().get(request.dirName);
            url = bind.getUrl();
            zip = bind.getZip();
        }
        return new UpdateResponse(dir, url, zip);
    }
}

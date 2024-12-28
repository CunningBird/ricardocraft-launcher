package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.interfaces.user.UserSupportAdditionalData;
import ru.ricardocraft.backend.dto.response.auth.AdditionalDataResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AdditionalDataRequest;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.util.Map;

@Component
public class AdditionalDataService extends AbstractService {

    @Autowired
    protected AdditionalDataService(ServerWebSocketHandler handler) {
        super(AdditionalDataRequest.class, handler);
    }

    @Override
    public AdditionalDataResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        AdditionalDataRequest request = castResponse(rawResponse);

        if (!client.isAuth) {
            throw new Exception("Access denied");
        }
        AuthProviderPair pair = client.auth;
        if (request.username == null && request.uuid == null) {
            Map<String, String> properties;
            User user = client.getUser();
            if (user instanceof UserSupportAdditionalData userSupport) {
                if (client.permissions.hasPerm("launchserver.request.addionaldata.privileged")) {
                    properties = userSupport.getPropertiesMap();
                } else {
                    properties = userSupport.getPropertiesMapUnprivilegedSelf();
                }
            } else {
                properties = Map.of();
            }
            return new AdditionalDataResponse(properties);
        }
        User user;
        if (request.username != null) {
            user = pair.core.getUserByUsername(request.username);
        } else {
            user = pair.core.getUserByUUID(request.uuid);
        }
        if (!(user instanceof UserSupportAdditionalData userSupport)) {
            return new AdditionalDataResponse(Map.of());
        }
        Map<String, String> properties;
        if (client.permissions.hasPerm("launchserver.request.addionaldata.privileged")) {
            properties = userSupport.getPropertiesMap();
        } else {
            properties = userSupport.getPropertiesMapUnprivileged();
        }
        return new AdditionalDataResponse(properties);
    }
}

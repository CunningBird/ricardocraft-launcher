package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.interfaces.user.UserSupportAdditionalData;
import ru.ricardocraft.backend.dto.events.request.auth.AdditionalDataRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AdditionalDataResponse;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

import java.util.Map;

@Component
public class AdditionalDataResponseService extends AbstractResponseService {

    @Autowired
    protected AdditionalDataResponseService(ServerWebSocketHandler handler) {
        super(AdditionalDataResponse.class, handler);
    }

    @Override
    public AdditionalDataRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        AdditionalDataResponse response = castResponse(rawResponse);

        if (!client.isAuth) {
            throw new Exception("Access denied");
        }
        AuthProviderPair pair = client.auth;
        if (response.username == null && response.uuid == null) {
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
            return new AdditionalDataRequestEvent(properties);
        }
        User user;
        if (response.username != null) {
            user = pair.core.getUserByUsername(response.username);
        } else {
            user = pair.core.getUserByUUID(response.uuid);
        }
        if (!(user instanceof UserSupportAdditionalData userSupport)) {
            return new AdditionalDataRequestEvent(Map.of());
        }
        Map<String, String> properties;
        if (client.permissions.hasPerm("launchserver.request.addionaldata.privileged")) {
            properties = userSupport.getPropertiesMap();
        } else {
            properties = userSupport.getPropertiesMapUnprivileged();
        }
        return new AdditionalDataRequestEvent(properties);
    }
}

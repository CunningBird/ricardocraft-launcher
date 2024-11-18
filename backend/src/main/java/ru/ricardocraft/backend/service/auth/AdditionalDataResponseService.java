package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.interfaces.user.UserSupportAdditionalData;
import ru.ricardocraft.backend.base.events.request.AdditionalDataRequestEvent;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.auth.AdditionalDataResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.util.Map;

@Component
public class AdditionalDataResponseService extends AbstractResponseService {

    @Autowired
    protected AdditionalDataResponseService(WebSocketService service) {
        super(AdditionalDataResponse.class, service);
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        AdditionalDataResponse response = castResponse(rawResponse);

        if (!client.isAuth) {
            sendError(ctx, "Access denied", response.requestUUID);
            return;
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
            sendResult(ctx, new AdditionalDataRequestEvent(properties), response.requestUUID);
            return;
        }
        User user;
        if (response.username != null) {
            user = pair.core.getUserByUsername(response.username);
        } else {
            user = pair.core.getUserByUUID(response.uuid);
        }
        if (!(user instanceof UserSupportAdditionalData userSupport)) {
            sendResult(ctx, new AdditionalDataRequestEvent(Map.of()), response.requestUUID);
            return;
        }
        Map<String, String> properties;
        if (client.permissions.hasPerm("launchserver.request.addionaldata.privileged")) {
            properties = userSupport.getPropertiesMap();
        } else {
            properties = userSupport.getPropertiesMapUnprivileged();
        }
        sendResult(ctx, new AdditionalDataRequestEvent(properties), response.requestUUID);
    }
}

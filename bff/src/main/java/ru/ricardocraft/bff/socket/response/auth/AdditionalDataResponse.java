package ru.ricardocraft.bff.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.AdditionalDataRequestEvent;
import ru.ricardocraft.bff.auth.AuthProviderPair;
import ru.ricardocraft.bff.auth.core.User;
import ru.ricardocraft.bff.auth.core.interfaces.user.UserSupportAdditionalData;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

import java.util.Map;
import java.util.UUID;

public class AdditionalDataResponse extends SimpleResponse {
    public String username;
    public UUID uuid;

    @Override
    public String getType() {
        return "additionalData";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!client.isAuth) {
            sendError("Access denied");
            return;
        }
        AuthProviderPair pair = client.auth;
        if (username == null && uuid == null) {
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
            sendResult(new AdditionalDataRequestEvent(properties));
            return;
        }
        User user;
        if (username != null) {
            user = pair.core.getUserByUsername(username);
        } else {
            user = pair.core.getUserByUUID(uuid);
        }
        if (!(user instanceof UserSupportAdditionalData userSupport)) {
            sendResult(new AdditionalDataRequestEvent(Map.of()));
            return;
        }
        Map<String, String> properties;
        if (client.permissions.hasPerm("launchserver.request.addionaldata.privileged")) {
            properties = userSupport.getPropertiesMap();
        } else {
            properties = userSupport.getPropertiesMapUnprivileged();
        }
        sendResult(new AdditionalDataRequestEvent(properties));
    }
}

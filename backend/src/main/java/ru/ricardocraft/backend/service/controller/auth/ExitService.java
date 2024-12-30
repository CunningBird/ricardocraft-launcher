package ru.ricardocraft.backend.service.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.request.auth.ExitRequest;
import ru.ricardocraft.backend.dto.response.auth.ExitResponse;
import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportExit;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Component
public class ExitService {

    public ExitResponse exit(ExitRequest request, ServerWebSocketHandler handler, WebSocketSession session, Client client) throws Exception {
        if (request.username != null && (!client.isAuth || client.permissions == null || !client.permissions.hasPerm("launchserver\\.management\\.kick"))) {
            throw new Exception("Permissions denied");
        }
        if (request.username == null) {
            if (!client.isAuth || client.auth == null) {
                throw new Exception("You are not authorized");
            }

            Client newClient = new Client();
            newClient.checkSign = client.checkSign;

            handler.updateSessionClient(session, newClient);

            AuthSupportExit supportExit = client.auth.core.isSupport(AuthSupportExit.class);
            if (supportExit != null) {
                if (request.exitAll) {
                    supportExit.exitUser(client.getUser());
                } else {
                    UserSession userSession = client.sessionObject;
                    if (userSession != null) {
                        supportExit.deleteSession(userSession);
                    }
                }
            }
            return new ExitResponse(ExitResponse.ExitReason.CLIENT);
        } else {
            handler.forEachActiveChannels(((webSocketSession, client1) -> {
                if (client1 != null && client.isAuth && client.username != null && client1.username.equals(request.username)) {
                    Client newCusClient = new Client();
                    newCusClient.checkSign = client1.checkSign;
                    if (client1.staticProperties != null) {
                        newCusClient.staticProperties = new HashMap<>(client1.staticProperties);
                    }
                    handler.updateSessionClient(webSocketSession, client1);
                    ExitResponse event = new ExitResponse(ExitResponse.ExitReason.SERVER);
                    event.requestUUID = AbstractResponse.eventUUID;

                    try {
                        handler.sendMessage(webSocketSession, event, false);
                    } catch (IOException e) {
                        log.error("Error occurred during sending message. Cause: {}", e.getMessage());
                    }
                }
            }));
            return new ExitResponse(ExitResponse.ExitReason.NO_EXIT);
        }
    }
}

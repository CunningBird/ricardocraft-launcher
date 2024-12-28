package ru.ricardocraft.backend.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportExit;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.response.auth.ExitResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.ExitRequest;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
@Component
public class ExitService extends AbstractService {

    @Autowired
    public ExitService(ServerWebSocketHandler handler) {
        super(ExitRequest.class, handler);
    }

    @Override
    public ExitResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        ExitRequest response = (ExitRequest) rawResponse;

        if (response.username != null && (!client.isAuth || client.permissions == null || !client.permissions.hasPerm("launchserver\\.management\\.kick"))) {
            throw new Exception("Permissions denied");
        }
        if (response.username == null) {
            if (!client.isAuth || client.auth == null) {
                throw new Exception("You are not authorized");
            }

            Client newClient = new Client();
            newClient.checkSign = client.checkSign;

            handler.updateSessionClient(session, newClient);

            AuthSupportExit supportExit = client.auth.core.isSupport(AuthSupportExit.class);
            if (supportExit != null) {
                if (response.exitAll) {
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
                if (client1 != null && client.isAuth && client.username != null && client1.username.equals(response.username)) {
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

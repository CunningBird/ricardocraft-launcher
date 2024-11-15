package ru.ricardocraft.backend.service.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportExit;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ExitRequestEvent;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.ExitResponse;

import java.util.HashMap;

@Component
public class ExitResponseService extends AbstractResponseService {

    @Autowired
    public ExitResponseService(WebSocketService service) {
        super(ExitResponse.class, service);
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        ExitResponse response = (ExitResponse) rawResponse;

        if (response.username != null && (!client.isAuth || client.permissions == null || !client.permissions.hasPerm("launchserver\\.management\\.kick"))) {
            sendError(ctx,"Permissions denied", response.requestUUID);
            return;
        }
        if (response.username == null) {
            if(!client.isAuth || client.auth == null) {
                sendError(ctx,"You are not authorized", response.requestUUID);
                return;
            }
            {
                WebSocketFrameHandler handler = ctx.pipeline().get(WebSocketFrameHandler.class);
                if (handler == null) {
                    sendError(ctx,"Exit internal error", response.requestUUID);
                    return;
                }
                Client newClient = new Client();
                newClient.checkSign = client.checkSign;
                handler.setClient(newClient);
                AuthSupportExit supportExit = client.auth.core.isSupport(AuthSupportExit.class);
                if (supportExit != null) {
                    if (response.exitAll) {
                        supportExit.exitUser(client.getUser());
                    } else {
                        UserSession session = client.sessionObject;
                        if (session != null) {
                            supportExit.deleteSession(session);
                        }
                    }
                }
                sendResult(ctx, new ExitRequestEvent(ExitRequestEvent.ExitReason.CLIENT), response.requestUUID);
            }
        } else {
            service.forEachActiveChannels(((channel, webSocketFrameHandler) -> {
                Client client1 = webSocketFrameHandler.getClient();
                if (client1 != null && client.isAuth && client.username != null && client1.username.equals(response.username)) {
                    exit(webSocketFrameHandler, channel, ExitRequestEvent.ExitReason.SERVER);
                }
            }));
            sendResult(ctx, new ExitRequestEvent(ExitRequestEvent.ExitReason.NO_EXIT), response.requestUUID);
        }
    }

    public void exit(WebSocketFrameHandler wsHandler, Channel channel, ExitRequestEvent.ExitReason reason) {
        Client chClient = wsHandler.getClient();
        Client newCusClient = new Client();
        newCusClient.checkSign = chClient.checkSign;
        if (chClient.staticProperties != null) {
            newCusClient.staticProperties = new HashMap<>(chClient.staticProperties);
        }
        wsHandler.setClient(newCusClient);
        ExitRequestEvent event = new ExitRequestEvent(reason);
        event.requestUUID = RequestEvent.eventUUID;
        wsHandler.service.sendObject(channel, event);
    }
}

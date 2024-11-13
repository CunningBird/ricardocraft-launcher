package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportExit;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ExitRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.WebSocketFrameHandler;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.util.HashMap;

public class ExitResponse extends SimpleResponse {
    public boolean exitAll;
    public String username;

    public static void exit(WebSocketFrameHandler wsHandler, Channel channel, ExitRequestEvent.ExitReason reason) {

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

    @Override
    public String getType() {
        return "exit";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (username != null && (!client.isAuth || client.permissions == null || !client.permissions.hasPerm("launchserver\\.management\\.kick"))) {
            sendError("Permissions denied");
            return;
        }
        if (username == null) {
            if(!client.isAuth || client.auth == null) {
                sendError("You are not authorized");
                return;
            }
            {
                WebSocketFrameHandler handler = ctx.pipeline().get(WebSocketFrameHandler.class);
                if (handler == null) {
                    sendError("Exit internal error");
                    return;
                }
                Client newClient = new Client();
                newClient.checkSign = client.checkSign;
                handler.setClient(newClient);
                AuthSupportExit supportExit = client.auth.core.isSupport(AuthSupportExit.class);
                if (supportExit != null) {
                    if (exitAll) {
                        supportExit.exitUser(client.getUser());
                    } else {
                        UserSession session = client.sessionObject;
                        if (session != null) {
                            supportExit.deleteSession(session);
                        }
                    }
                }
                sendResult(new ExitRequestEvent(ExitRequestEvent.ExitReason.CLIENT));
            }
        } else {
            service.forEachActiveChannels(((channel, webSocketFrameHandler) -> {
                Client client1 = webSocketFrameHandler.getClient();
                if (client1 != null && client.isAuth && client.username != null && client1.username.equals(username)) {
                    exit(webSocketFrameHandler, channel, ExitRequestEvent.ExitReason.SERVER);
                }
            }));
            sendResult(new ExitRequestEvent(ExitRequestEvent.ExitReason.NO_EXIT));
        }
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
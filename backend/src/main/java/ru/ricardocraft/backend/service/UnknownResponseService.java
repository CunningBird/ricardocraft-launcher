package ru.ricardocraft.backend.service;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.UnknownResponse;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;

@Component
public class UnknownResponseService extends AbstractResponseService {

    @Autowired
    public UnknownResponseService(WebSocketService service) {
        super(UnknownResponse.class, service);
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        UnknownResponse response = (UnknownResponse) rawResponse;

        sendError(ctx, "This type of request is not supported", response.requestUUID);
    }
}
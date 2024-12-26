package ru.ricardocraft.backend.service;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.events.request.update.UpdateRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.UnknownResponse;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class UnknownResponseService extends AbstractResponseService {

    @Autowired
    public UnknownResponseService(WebSocketService service) {
        super(UnknownResponse.class, service);
    }

    @Override
    public UpdateRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        throw new Exception("This type of request is not supported");
    }
}

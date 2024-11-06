package ru.ricardocraft.backend.socket.response;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.socket.Client;

public class UnknownResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "unknown";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        sendError("This type of request is not supported");
    }
}

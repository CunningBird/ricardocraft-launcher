package ru.ricardocraft.bff.socket.response;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.bff.socket.Client;

public interface WebSocketServerResponse extends WebSocketRequest {
    String getType();

    void execute(ChannelHandlerContext ctx, Client client) throws Exception;

    default ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ;
    }

    enum ThreadSafeStatus {
        NONE, READ, READ_WRITE
    }
}

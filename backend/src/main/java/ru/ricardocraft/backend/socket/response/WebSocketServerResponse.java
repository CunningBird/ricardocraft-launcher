package ru.ricardocraft.backend.socket.response;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.socket.Client;

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

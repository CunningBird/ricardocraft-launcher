package ru.ricardocraft.backend.socket.response;

import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;

public interface WebSocketServerResponse extends WebSocketRequest {
    String getType();

    default ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ;
    }

    enum ThreadSafeStatus {
        NONE, READ, READ_WRITE
    }
}

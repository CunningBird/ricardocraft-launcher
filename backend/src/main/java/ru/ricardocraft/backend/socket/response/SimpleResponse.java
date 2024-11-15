package ru.ricardocraft.backend.socket.response;

import java.util.UUID;

public abstract class SimpleResponse implements WebSocketServerResponse {
    public UUID requestUUID;
    public transient UUID connectUUID;
    public transient String ip;
}

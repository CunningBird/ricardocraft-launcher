package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

import java.util.UUID;

public class GetConnectUUIDRequestEvent extends RequestEvent {
    public UUID connectUUID;
    public int shardId;

    public GetConnectUUIDRequestEvent(UUID connectUUID, int shardId) {
        this.connectUUID = connectUUID;
        this.shardId = shardId;
    }

    @Override
    public String getType() {
        return "getConnectUUID";
    }
}

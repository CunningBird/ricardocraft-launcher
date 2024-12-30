package ru.ricardocraft.backend.dto.response.management;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.UUID;

public class GetConnectUUIDResponse extends AbstractResponse {
    public UUID connectUUID;
    public int shardId;

    public GetConnectUUIDResponse(UUID connectUUID, int shardId) {
        this.connectUUID = connectUUID;
        this.shardId = shardId;
    }

    @Override
    public String getType() {
        return "getConnectUUID";
    }
}

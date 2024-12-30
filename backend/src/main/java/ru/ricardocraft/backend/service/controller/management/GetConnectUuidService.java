package ru.ricardocraft.backend.service.controller.management;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.backend.dto.response.management.GetConnectUUIDResponse;

@Component
public class GetConnectUuidService {

    private final int shardId = Integer.parseInt(System.getProperty("launchserver.shardId", "0"));

    public GetConnectUUIDResponse getConnectUuid(GetConnectUUIDRequest request) {
        return new GetConnectUUIDResponse(request.connectUUID, shardId);
    }
}

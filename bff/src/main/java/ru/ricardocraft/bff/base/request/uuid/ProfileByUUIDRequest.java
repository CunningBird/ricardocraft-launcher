package ru.ricardocraft.bff.base.request.uuid;

import ru.ricardocraft.bff.base.events.request.ProfileByUUIDRequestEvent;
import ru.ricardocraft.bff.base.request.Request;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

import java.util.Objects;
import java.util.UUID;

public final class ProfileByUUIDRequest extends Request<ProfileByUUIDRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final UUID uuid;


    public ProfileByUUIDRequest(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    @Override
    public String getType() {
        return "profileByUUID";
    }
}

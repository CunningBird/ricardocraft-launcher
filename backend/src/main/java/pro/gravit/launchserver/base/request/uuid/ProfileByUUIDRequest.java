package pro.gravit.launchserver.base.request.uuid;

import pro.gravit.launchserver.base.events.request.ProfileByUUIDRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;
import pro.gravit.launchserver.core.LauncherNetworkAPI;

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

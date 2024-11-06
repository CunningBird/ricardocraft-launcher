package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.events.request.JoinServerRequestEvent;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.helper.VerifyHelper;

import java.util.UUID;

public final class JoinServerRequest extends Request<JoinServerRequestEvent> implements WebSocketRequest {

    // Instance
    @LauncherNetworkAPI
    public final String username;
    @LauncherNetworkAPI
    public final UUID uuid;
    @LauncherNetworkAPI
    public final String accessToken;
    @LauncherNetworkAPI
    public final String serverID;


    public JoinServerRequest(String username, String accessToken, String serverID) {
        this.username = username;
        this.uuid = null;
        this.accessToken = accessToken;
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }

    public JoinServerRequest(UUID uuid, String accessToken, String serverID) {
        this.username = null;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.serverID = serverID;
    }

    @Override
    public String getType() {
        return "joinServer";
    }
}

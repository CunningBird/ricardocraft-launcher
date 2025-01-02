package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.client.WebSocketRequest;
import ru.ricardocraft.client.dto.response.JoinServerRequestEvent;
import ru.ricardocraft.client.base.helper.VerifyHelper;

import java.util.UUID;

public final class JoinServerRequest extends Request<JoinServerRequestEvent> implements WebSocketRequest {

    // Instance
    public final String username;
    public final UUID uuid;
    public final String accessToken;
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

package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.client.WebSocketRequest;
import ru.ricardocraft.client.dto.response.CheckServerRequestEvent;
import ru.ricardocraft.client.base.helper.VerifyHelper;

public final class CheckServerRequest extends Request<CheckServerRequestEvent> implements WebSocketRequest {

    public final String username;
    public final String serverID;
    public boolean needHardware;
    public boolean needProperties;


    public CheckServerRequest(String username, String serverID) {
        this.username = username;
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }

    public CheckServerRequest(String username, String serverID, boolean needHardware, boolean needProperties) {
        this.username = username;
        this.serverID = VerifyHelper.verifyServerID(serverID);
        this.needHardware = needHardware;
        this.needProperties = needProperties;
    }

    @Override
    public String getType() {
        return "checkServer";
    }
}

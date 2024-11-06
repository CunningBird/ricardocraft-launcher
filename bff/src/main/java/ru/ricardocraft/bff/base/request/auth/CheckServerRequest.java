package ru.ricardocraft.bff.base.request.auth;

import ru.ricardocraft.bff.core.LauncherNetworkAPI;
import ru.ricardocraft.bff.base.events.request.CheckServerRequestEvent;
import ru.ricardocraft.bff.base.request.Request;
import ru.ricardocraft.bff.base.request.websockets.WebSocketRequest;
import ru.ricardocraft.bff.helper.VerifyHelper;

public final class CheckServerRequest extends Request<CheckServerRequestEvent> implements WebSocketRequest {
    @LauncherNetworkAPI
    public final String username;
    @LauncherNetworkAPI
    public final String serverID;
    @LauncherNetworkAPI
    public boolean needHardware;
    @LauncherNetworkAPI
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

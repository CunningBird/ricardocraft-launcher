package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.core.LauncherNetworkAPI;
import pro.gravit.launchserver.base.events.request.CheckServerRequestEvent;
import pro.gravit.launchserver.base.request.Request;
import pro.gravit.launchserver.base.request.websockets.WebSocketRequest;
import pro.gravit.launchserver.helper.VerifyHelper;

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

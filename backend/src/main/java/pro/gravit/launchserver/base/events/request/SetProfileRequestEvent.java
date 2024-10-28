package pro.gravit.launchserver.base.events.request;

import pro.gravit.launchserver.base.events.RequestEvent;
import pro.gravit.launchserver.base.profiles.ClientProfile;
import pro.gravit.launchserver.core.LauncherNetworkAPI;

import java.util.UUID;


public class SetProfileRequestEvent extends RequestEvent {
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("08c0de9e-4364-4152-9066-8354a3a48541");
    @LauncherNetworkAPI
    public final ClientProfile newProfile;

    public SetProfileRequestEvent(ClientProfile newProfile) {
        this.newProfile = newProfile;
    }

    @Override
    public String getType() {
        return "setProfile";
    }
}

package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.client.ClientParams;

import java.util.Collection;

public class ClientProcessPreInvokeMainClassEvent extends LauncherModule.Event {
    public final ClientParams params;
    public final ClientProfile profile;
    public final Collection<String> args;

    public ClientProcessPreInvokeMainClassEvent(ClientParams params, ClientProfile profile, Collection<String> args) {
        this.params = params;
        this.profile = profile;
        this.args = args;
    }
}

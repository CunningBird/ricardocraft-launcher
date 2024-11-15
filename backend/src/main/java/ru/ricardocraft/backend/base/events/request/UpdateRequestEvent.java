package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.core.hasher.HashedDir;

public class UpdateRequestEvent extends RequestEvent {
    @LauncherNetworkAPI
    public final HashedDir hdir;
    @LauncherNetworkAPI
    public final boolean zip;
    @LauncherNetworkAPI
    public String url;

    public UpdateRequestEvent(HashedDir hdir, String url, boolean zip) {
        this.hdir = hdir;
        this.url = url;
        this.zip = zip;
    }

    @Override
    public String getType() {
        return "update";
    }
}

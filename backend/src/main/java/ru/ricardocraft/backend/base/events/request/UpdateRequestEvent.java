package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.core.hasher.HashedDir;
import ru.ricardocraft.backend.base.events.RequestEvent;

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

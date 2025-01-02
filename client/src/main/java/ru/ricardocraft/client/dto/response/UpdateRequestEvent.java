package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.base.hasher.HashedDir;
import ru.ricardocraft.client.dto.RequestEvent;

public class UpdateRequestEvent extends RequestEvent {

    public final HashedDir hdir;
    public final boolean zip;
    public String url;
    public boolean fullDownload;

    public UpdateRequestEvent(HashedDir hdir) {
        this.hdir = hdir;
        this.zip = false;
    }

    public UpdateRequestEvent(HashedDir hdir, String url) {
        this.hdir = hdir;
        this.url = url;
        this.zip = false;
    }

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

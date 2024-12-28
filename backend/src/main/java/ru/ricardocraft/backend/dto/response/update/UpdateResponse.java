package ru.ricardocraft.backend.dto.response.update;

import ru.ricardocraft.backend.base.core.LauncherNetworkAPI;
import ru.ricardocraft.backend.base.hasher.HashedDir;
import ru.ricardocraft.backend.dto.AbstractResponse;

public class UpdateResponse extends AbstractResponse {
    @LauncherNetworkAPI
    public final HashedDir hdir;
    @LauncherNetworkAPI
    public final boolean zip;
    @LauncherNetworkAPI
    public String url;

    public UpdateResponse(HashedDir hdir, String url, boolean zip) {
        this.hdir = hdir;
        this.url = url;
        this.zip = zip;
    }

    @Override
    public String getType() {
        return "update";
    }
}

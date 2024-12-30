package ru.ricardocraft.backend.dto.response.update;

import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.ExtendedTokenResponse;

import java.util.UUID;


public class LauncherResponse extends AbstractResponse implements ExtendedTokenResponse {
    public static final String LAUNCHER_EXTENDED_TOKEN_NAME = "launcher";
    @SuppressWarnings("unused")
    private static final UUID uuid = UUID.fromString("d54cc12a-4f59-4f23-9b10-f527fdd2e38f");
    public String url;
    public byte[] digest;
    public byte[] binary;
    public boolean needUpdate;
    public String launcherExtendedToken;
    public long launcherExtendedTokenExpire;

    public LauncherResponse(boolean needUpdate, String url) {
        this.needUpdate = needUpdate;
        this.url = url;
    }

    public LauncherResponse(boolean needUpdate, String url, String launcherExtendedToken, long expire) {
        this.url = url;
        this.needUpdate = needUpdate;
        this.launcherExtendedToken = launcherExtendedToken;
        this.launcherExtendedTokenExpire = expire;
    }

    @Override
    public String getType() {
        return "launcher";
    }

    @Override
    public String getExtendedTokenName() {
        return "launcher";
    }

    @Override
    public String getExtendedToken() {
        return launcherExtendedToken;
    }

    @Override
    public long getExtendedTokenExpire() {
        return launcherExtendedTokenExpire;
    }
}

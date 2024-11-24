package ru.ricardocraft.client.base.events.request;

import ru.ricardocraft.client.base.events.RequestEvent;

import java.util.Set;

public class AssetUploadInfoRequestEvent extends RequestEvent {
    public Set<String> available;
    public SlimSupportConf slimSupportConf;

    public AssetUploadInfoRequestEvent(Set<String> available, SlimSupportConf slimSupportConf) {
        this.available = available;
        this.slimSupportConf = slimSupportConf;
    }

    @Override
    public String getType() {
        return "assetUploadInfo";
    }

    public enum SlimSupportConf {
        UNSUPPORTED, USER, SERVER
    }
}
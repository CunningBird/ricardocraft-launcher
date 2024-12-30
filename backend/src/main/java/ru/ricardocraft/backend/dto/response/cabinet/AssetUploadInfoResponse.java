package ru.ricardocraft.backend.dto.response.cabinet;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.Set;

public class AssetUploadInfoResponse extends AbstractResponse {
    public Set<String> available;
    public SlimSupportConf slimSupportConf;

    public AssetUploadInfoResponse(Set<String> available, SlimSupportConf slimSupportConf) {
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

package ru.ricardocraft.client.dto.request.cabinet;

import ru.ricardocraft.client.dto.response.AssetUploadInfoRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

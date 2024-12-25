package ru.ricardocraft.client.base.request.cabinet;

import ru.ricardocraft.client.base.events.request.AssetUploadInfoRequestEvent;
import ru.ricardocraft.client.base.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

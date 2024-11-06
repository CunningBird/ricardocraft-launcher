package ru.ricardocraft.backend.base.request.cabinet;

import ru.ricardocraft.backend.base.events.request.AssetUploadInfoRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

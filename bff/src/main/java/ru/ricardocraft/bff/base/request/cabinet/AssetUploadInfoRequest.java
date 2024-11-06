package ru.ricardocraft.bff.base.request.cabinet;

import ru.ricardocraft.bff.base.events.request.AssetUploadInfoRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

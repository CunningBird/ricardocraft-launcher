package ru.ricardocraft.backend.base.request.cabinet;

import ru.ricardocraft.backend.base.events.request.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class GetAssetUploadUrl extends Request<GetAssetUploadUrlRequestEvent> {
    public String name;

    public GetAssetUploadUrl() {
    }

    public GetAssetUploadUrl(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

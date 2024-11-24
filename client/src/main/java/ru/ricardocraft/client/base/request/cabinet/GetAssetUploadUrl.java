package ru.ricardocraft.client.base.request.cabinet;

import ru.ricardocraft.client.base.events.request.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.client.base.request.Request;

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

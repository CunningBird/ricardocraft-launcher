package ru.ricardocraft.client.dto.request.cabinet;

import ru.ricardocraft.client.dto.response.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class GetAssetUploadUrl extends Request<GetAssetUploadUrlRequestEvent> {
    public String name;

    public GetAssetUploadUrl(String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

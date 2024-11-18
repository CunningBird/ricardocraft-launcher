package ru.ricardocraft.backend.dto.cabinet;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class GetAssetUploadInfoResponse extends SimpleResponse {

    public String name;

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

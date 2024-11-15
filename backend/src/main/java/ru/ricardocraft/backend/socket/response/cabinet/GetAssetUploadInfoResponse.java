package ru.ricardocraft.backend.socket.response.cabinet;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetAssetUploadInfoResponse extends SimpleResponse {

    public String name;

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

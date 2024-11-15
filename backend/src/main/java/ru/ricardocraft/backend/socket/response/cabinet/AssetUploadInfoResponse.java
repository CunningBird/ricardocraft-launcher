package ru.ricardocraft.backend.socket.response.cabinet;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class AssetUploadInfoResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

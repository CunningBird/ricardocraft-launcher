package ru.ricardocraft.backend.dto.response.cabinet;

import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;

public class GetAssetUploadUrlResponse extends AbstractResponse {
    public static final String FEATURE_NAME = "assetupload";
    public String url;
    public AuthResponse.OAuthRequestEvent token;

    public GetAssetUploadUrlResponse(String url, AuthResponse.OAuthRequestEvent token) {
        this.url = url;
        this.token = token;
    }

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

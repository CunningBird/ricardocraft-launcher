package ru.ricardocraft.backend.dto.events.request.cabinet;

import ru.ricardocraft.backend.dto.events.RequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;

public class GetAssetUploadUrlRequestEvent extends RequestEvent {
    public static final String FEATURE_NAME = "assetupload";
    public String url;
    public AuthRequestEvent.OAuthRequestEvent token;

    public GetAssetUploadUrlRequestEvent(String url, AuthRequestEvent.OAuthRequestEvent token) {
        this.url = url;
        this.token = token;
    }

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

package pro.gravit.launcher.gui.base.events.request;

import pro.gravit.launcher.gui.base.events.RequestEvent;
import pro.gravit.launcher.gui.base.events.request.AuthRequestEvent;

public class GetAssetUploadUrlRequestEvent extends RequestEvent {
    public static final String FEATURE_NAME = "assetupload";
    public String url;
    public pro.gravit.launcher.gui.base.events.request.AuthRequestEvent.OAuthRequestEvent token;

    public GetAssetUploadUrlRequestEvent() {
    }

    public GetAssetUploadUrlRequestEvent(String url, AuthRequestEvent.OAuthRequestEvent token) {
        this.url = url;
        this.token = token;
    }

    @Override
    public String getType() {
        return "getAssetUploadUrl";
    }
}

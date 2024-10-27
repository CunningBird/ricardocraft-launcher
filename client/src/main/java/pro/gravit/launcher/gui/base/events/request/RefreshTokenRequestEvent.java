package pro.gravit.launcher.gui.base.events.request;

import pro.gravit.launcher.gui.base.events.RequestEvent;
import pro.gravit.launcher.gui.base.events.request.AuthRequestEvent;

public class RefreshTokenRequestEvent extends RequestEvent {
    public pro.gravit.launcher.gui.base.events.request.AuthRequestEvent.OAuthRequestEvent oauth;

    public RefreshTokenRequestEvent(AuthRequestEvent.OAuthRequestEvent oauth) {
        this.oauth = oauth;
    }

    @Override
    public String getType() {
        return "refreshToken";
    }
}

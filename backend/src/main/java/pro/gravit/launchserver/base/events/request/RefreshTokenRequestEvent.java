package pro.gravit.launchserver.base.events.request;

import pro.gravit.launchserver.base.events.RequestEvent;

public class RefreshTokenRequestEvent extends RequestEvent {
    public AuthRequestEvent.OAuthRequestEvent oauth;

    public RefreshTokenRequestEvent(AuthRequestEvent.OAuthRequestEvent oauth) {
        this.oauth = oauth;
    }

    @Override
    public String getType() {
        return "refreshToken";
    }
}

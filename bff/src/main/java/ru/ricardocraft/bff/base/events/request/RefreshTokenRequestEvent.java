package ru.ricardocraft.bff.base.events.request;

import ru.ricardocraft.bff.base.events.RequestEvent;

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

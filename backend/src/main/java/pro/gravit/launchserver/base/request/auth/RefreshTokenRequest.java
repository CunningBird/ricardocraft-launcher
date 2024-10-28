package pro.gravit.launchserver.base.request.auth;

import pro.gravit.launchserver.base.events.request.RefreshTokenRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class RefreshTokenRequest extends Request<RefreshTokenRequestEvent> {
    public String authId;
    public String refreshToken;

    public RefreshTokenRequest(String authId, String refreshToken) {
        this.authId = authId;
        this.refreshToken = refreshToken;
    }

    @Override
    public String getType() {
        return "refreshToken";
    }
}

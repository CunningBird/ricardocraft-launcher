package pro.gravit.launcher.gui.base.request.auth;

import pro.gravit.launcher.gui.base.events.request.RefreshTokenRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

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

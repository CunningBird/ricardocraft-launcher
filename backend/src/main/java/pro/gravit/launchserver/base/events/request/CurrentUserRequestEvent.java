package pro.gravit.launchserver.base.events.request;

import pro.gravit.launchserver.base.ClientPermissions;
import pro.gravit.launchserver.base.events.RequestEvent;
import pro.gravit.launchserver.base.profiles.PlayerProfile;

public class CurrentUserRequestEvent extends RequestEvent {
    public final UserInfo userInfo;

    public CurrentUserRequestEvent(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    @Override
    public String getType() {
        return "currentUser";
    }

    public static class UserInfo {
        public ClientPermissions permissions;
        public String accessToken;
        public PlayerProfile playerProfile;
    }
}

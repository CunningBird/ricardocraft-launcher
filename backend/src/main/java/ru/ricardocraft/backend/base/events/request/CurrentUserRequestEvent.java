package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.profiles.PlayerProfile;

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

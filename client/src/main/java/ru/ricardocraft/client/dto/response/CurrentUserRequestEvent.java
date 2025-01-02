package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.service.ClientPermissions;
import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.service.profiles.PlayerProfile;

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

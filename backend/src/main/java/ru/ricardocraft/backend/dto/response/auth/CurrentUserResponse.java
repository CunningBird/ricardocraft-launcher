package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.profiles.PlayerProfile;

public class CurrentUserResponse extends AbstractResponse {
    public final UserInfo userInfo;

    public CurrentUserResponse(UserInfo userInfo) {
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

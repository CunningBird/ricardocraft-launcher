package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.List;

public class RestoreResponse extends AbstractResponse {
    public CurrentUserResponse.UserInfo userInfo;
    public List<String> invalidTokens;

    public RestoreResponse(CurrentUserResponse.UserInfo userInfo, List<String> invalidTokens) {
        this.userInfo = userInfo;
        this.invalidTokens = invalidTokens;
    }

    public RestoreResponse(List<String> invalidTokens) {
        this.invalidTokens = invalidTokens;
    }

    @Override
    public String getType() {
        return "restore";
    }
}

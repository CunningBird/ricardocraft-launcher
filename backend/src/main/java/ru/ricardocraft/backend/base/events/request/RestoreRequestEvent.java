package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;

import java.util.List;

public class RestoreRequestEvent extends RequestEvent {
    public CurrentUserRequestEvent.UserInfo userInfo;
    public List<String> invalidTokens;

    public RestoreRequestEvent(CurrentUserRequestEvent.UserInfo userInfo, List<String> invalidTokens) {
        this.userInfo = userInfo;
        this.invalidTokens = invalidTokens;
    }

    public RestoreRequestEvent(List<String> invalidTokens) {
        this.invalidTokens = invalidTokens;
    }

    @Override
    public String getType() {
        return "restore";
    }
}

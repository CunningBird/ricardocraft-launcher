package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

import java.util.Map;

public class RestoreResponse extends SimpleResponse {
    public String authId;
    public String accessToken;
    public Map<String, String> extended;
    public boolean needUserInfo;

    @Override
    public String getType() {
        return "restore";
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

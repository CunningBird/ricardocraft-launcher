package ru.ricardocraft.backend.dto.socket.auth;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

import java.util.Map;

public class RestoreResponse extends SimpleResponse {
    public String authId;
    public String accessToken;
    public Map<String, String> extended;
    public boolean needUserInfo;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

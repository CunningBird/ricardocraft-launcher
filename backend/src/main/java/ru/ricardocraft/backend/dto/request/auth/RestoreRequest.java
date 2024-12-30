package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

import java.util.Map;

public class RestoreRequest extends AbstractRequest {
    public String authId;
    public String accessToken;
    public Map<String, String> extended;
    public boolean needUserInfo;
}

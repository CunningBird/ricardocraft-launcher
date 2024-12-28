package ru.ricardocraft.backend.base.request.auth;

import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.dto.response.auth.RestoreResponse;

import java.util.Map;

public class RestoreRequest extends Request<RestoreResponse> {
    public String authId;
    public String accessToken;
    public Map<String, String> extended;
    public boolean needUserInfo;

    public RestoreRequest() {
    }

    public RestoreRequest(String authId, String accessToken, Map<String, String> extended, boolean needUserInfo) {
        this.authId = authId;
        this.accessToken = accessToken;
        this.extended = extended;
        this.needUserInfo = needUserInfo;
    }

    @Override
    public String getType() {
        return "restore";
    }
}

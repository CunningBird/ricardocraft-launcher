package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.Map;

public class AdditionalDataResponse extends AbstractResponse {
    public Map<String, String> data;

    public AdditionalDataResponse(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String getType() {
        return "additionalData";
    }
}

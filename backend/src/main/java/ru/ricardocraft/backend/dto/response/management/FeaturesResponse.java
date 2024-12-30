package ru.ricardocraft.backend.dto.response.management;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.Map;

public class FeaturesResponse extends AbstractResponse {
    public Map<String, String> features;

    public FeaturesResponse(Map<String, String> features) {
        this.features = features;
    }

    @Override
    public String getType() {
        return "features";
    }
}

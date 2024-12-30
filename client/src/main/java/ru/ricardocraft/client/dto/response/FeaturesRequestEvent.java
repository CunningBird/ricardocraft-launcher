package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

import java.util.Map;

public class FeaturesRequestEvent extends RequestEvent {
    public Map<String, String> features;

    public FeaturesRequestEvent() {
    }

    @Override
    public String getType() {
        return "features";
    }
}

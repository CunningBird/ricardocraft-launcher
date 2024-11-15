package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;

import java.util.Map;

public class FeaturesRequestEvent extends RequestEvent {
    public Map<String, String> features;

    public FeaturesRequestEvent(Map<String, String> features) {
        this.features = features;
    }

    @Override
    public String getType() {
        return "features";
    }
}

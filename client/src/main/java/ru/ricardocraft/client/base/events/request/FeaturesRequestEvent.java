package ru.ricardocraft.client.base.events.request;

import ru.ricardocraft.client.base.events.RequestEvent;

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

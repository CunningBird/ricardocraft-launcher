package ru.ricardocraft.backend.base.events.request;

import ru.ricardocraft.backend.base.events.RequestEvent;

import java.util.Map;

public class AdditionalDataRequestEvent extends RequestEvent {
    public Map<String, String> data;

    public AdditionalDataRequestEvent(Map<String, String> data) {
        this.data = data;
    }

    @Override
    public String getType() {
        return "additionalData";
    }
}

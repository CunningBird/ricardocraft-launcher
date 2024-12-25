package ru.ricardocraft.backend.dto.events.request.auth;

import ru.ricardocraft.backend.dto.events.RequestEvent;

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

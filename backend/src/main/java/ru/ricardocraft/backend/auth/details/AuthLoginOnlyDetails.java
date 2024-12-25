package ru.ricardocraft.backend.auth.details;

import ru.ricardocraft.backend.dto.events.request.auth.GetAvailabilityAuthRequestEvent;

public class AuthLoginOnlyDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "loginonly";
    }
}

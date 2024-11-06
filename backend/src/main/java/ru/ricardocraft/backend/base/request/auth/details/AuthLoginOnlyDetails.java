package ru.ricardocraft.backend.base.request.auth.details;

import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;

public class AuthLoginOnlyDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "loginonly";
    }
}

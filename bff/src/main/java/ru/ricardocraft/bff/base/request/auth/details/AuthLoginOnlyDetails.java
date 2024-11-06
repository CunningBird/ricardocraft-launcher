package ru.ricardocraft.bff.base.request.auth.details;

import ru.ricardocraft.bff.base.events.request.GetAvailabilityAuthRequestEvent;

public class AuthLoginOnlyDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "loginonly";
    }
}

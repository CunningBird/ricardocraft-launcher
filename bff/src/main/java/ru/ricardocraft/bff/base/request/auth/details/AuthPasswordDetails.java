package ru.ricardocraft.bff.base.request.auth.details;

import ru.ricardocraft.bff.base.events.request.GetAvailabilityAuthRequestEvent;

public class AuthPasswordDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "password";
    }


}

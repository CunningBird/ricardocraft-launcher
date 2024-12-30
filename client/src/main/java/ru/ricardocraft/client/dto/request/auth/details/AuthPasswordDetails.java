package ru.ricardocraft.client.dto.request.auth.details;

import ru.ricardocraft.client.dto.response.GetAvailabilityAuthRequestEvent;

public class AuthPasswordDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "password";
    }


}

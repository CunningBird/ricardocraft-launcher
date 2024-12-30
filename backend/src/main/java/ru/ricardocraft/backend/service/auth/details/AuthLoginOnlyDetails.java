package ru.ricardocraft.backend.service.auth.details;

import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;

public class AuthLoginOnlyDetails implements GetAvailabilityAuthResponse.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "loginonly";
    }
}

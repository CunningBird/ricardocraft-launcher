package ru.ricardocraft.backend.service.auth.details;

import ru.ricardocraft.backend.dto.response.auth.GetAvailabilityAuthResponse;

public class AuthPasswordDetails implements GetAvailabilityAuthResponse.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "password";
    }


}

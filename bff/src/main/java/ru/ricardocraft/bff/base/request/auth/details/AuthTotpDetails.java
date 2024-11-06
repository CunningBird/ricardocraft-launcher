package ru.ricardocraft.bff.base.request.auth.details;

import ru.ricardocraft.bff.base.events.request.GetAvailabilityAuthRequestEvent;

public class AuthTotpDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    public final String alg;
    public final int maxKeyLength;

    public AuthTotpDetails(String alg, int maxKeyLength) {
        this.alg = alg;
        this.maxKeyLength = maxKeyLength;
    }

    public AuthTotpDetails(String alg) {
        this.alg = alg;
        this.maxKeyLength = 6;
    }

    @Override
    public String getType() {
        return "totp";
    }
}

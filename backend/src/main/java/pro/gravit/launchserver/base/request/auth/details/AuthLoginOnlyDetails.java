package pro.gravit.launchserver.base.request.auth.details;

import pro.gravit.launchserver.base.events.request.GetAvailabilityAuthRequestEvent;

public class AuthLoginOnlyDetails implements GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails {
    @Override
    public String getType() {
        return "loginonly";
    }
}

package ru.ricardocraft.backend.base.request.management;

import ru.ricardocraft.backend.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.backend.base.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

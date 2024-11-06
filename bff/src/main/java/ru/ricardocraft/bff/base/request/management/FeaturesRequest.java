package ru.ricardocraft.bff.base.request.management;

import ru.ricardocraft.bff.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.bff.base.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

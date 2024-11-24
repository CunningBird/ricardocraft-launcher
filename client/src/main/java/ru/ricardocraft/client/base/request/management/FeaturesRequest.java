package ru.ricardocraft.client.base.request.management;

import ru.ricardocraft.client.base.events.request.FeaturesRequestEvent;
import ru.ricardocraft.client.base.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

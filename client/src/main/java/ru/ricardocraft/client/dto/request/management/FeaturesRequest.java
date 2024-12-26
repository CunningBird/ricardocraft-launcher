package ru.ricardocraft.client.dto.request.management;

import ru.ricardocraft.client.dto.response.FeaturesRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

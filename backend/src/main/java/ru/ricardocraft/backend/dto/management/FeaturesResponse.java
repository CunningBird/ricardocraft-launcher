package ru.ricardocraft.backend.dto.management;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class FeaturesResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "features";
    }
}

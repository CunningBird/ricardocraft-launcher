package ru.ricardocraft.backend.service.controller.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.response.management.FeaturesResponse;
import ru.ricardocraft.backend.service.FeaturesService;

@Component
@RequiredArgsConstructor
public class FeaturesRequestService {

    private final FeaturesService featuresService;

    public FeaturesResponse features() {
        return new FeaturesResponse(featuresService.getMap());
    }
}

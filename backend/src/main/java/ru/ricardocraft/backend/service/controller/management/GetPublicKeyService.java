package ru.ricardocraft.backend.service.controller.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.service.KeyAgreementService;

@Component
@RequiredArgsConstructor
public class GetPublicKeyService {

    private final KeyAgreementService keyAgreementService;

    public GetPublicKeyResponse getPublicKey() {
        return new GetPublicKeyResponse(keyAgreementService.rsaPublicKey, keyAgreementService.ecdsaPublicKey);
    }
}

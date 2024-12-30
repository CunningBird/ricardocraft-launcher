package ru.ricardocraft.backend.service.controller.cabinet;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.cabinet.GetAssetUploadInfoRequest;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadUrlResponse;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportAssetUpload;

@Component
public class GetAssetUploadInfoService {

    public GetAssetUploadUrlResponse getAssetUploadUrl(GetAssetUploadInfoRequest request, Client client) throws Exception {
        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            throw new Exception("Access denied");
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            throw new Exception("Not supported");
        }
        return new GetAssetUploadUrlResponse(support.getAssetUploadUrl(request.name, client.getUser()), support.getAssetUploadToken(request.name, client.getUser()));
    }
}

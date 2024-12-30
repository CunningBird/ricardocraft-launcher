package ru.ricardocraft.backend.service.controller.cabinet;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportAssetUpload;

@Component
public class AssetUploadInfoService {

    public AssetUploadInfoResponse assetUploadInfo(Client client) throws Exception {
        if (!client.isAuth || client.auth == null || client.getUser() == null) {
            throw new Exception("Access denied");
        }
        var support = client.auth.isSupport(AuthSupportAssetUpload.class);
        if (support == null) {
            throw new Exception("Not supported");
        }
        return support.getAssetUploadInfo(client.getUser());
    }
}

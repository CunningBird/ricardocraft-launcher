package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.Feature;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.dto.response.cabinet.GetAssetUploadUrlResponse;
import ru.ricardocraft.backend.repository.User;

import java.util.Set;

@Feature(GetAssetUploadUrlResponse.FEATURE_NAME)
public interface AuthSupportAssetUpload extends AuthSupport {
    String getAssetUploadUrl(String name, User user);

    default AuthResponse.OAuthRequestEvent getAssetUploadToken(String name, User user) {
        return null;
    }

    default AssetUploadInfoResponse getAssetUploadInfo(User user) {
        return new AssetUploadInfoResponse(Set.of("SKIN", "CAPE"), AssetUploadInfoResponse.SlimSupportConf.USER);
    }
}

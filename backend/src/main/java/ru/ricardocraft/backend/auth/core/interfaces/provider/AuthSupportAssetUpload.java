package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.Feature;
import ru.ricardocraft.backend.dto.events.request.cabinet.AssetUploadInfoRequestEvent;
import ru.ricardocraft.backend.dto.events.request.auth.AuthRequestEvent;
import ru.ricardocraft.backend.dto.events.request.cabinet.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.backend.repository.User;

import java.util.Set;

@Feature(GetAssetUploadUrlRequestEvent.FEATURE_NAME)
public interface AuthSupportAssetUpload extends AuthSupport {
    String getAssetUploadUrl(String name, User user);

    default AuthRequestEvent.OAuthRequestEvent getAssetUploadToken(String name, User user) {
        return null;
    }

    default AssetUploadInfoRequestEvent getAssetUploadInfo(User user) {
        return new AssetUploadInfoRequestEvent(Set.of("SKIN", "CAPE"), AssetUploadInfoRequestEvent.SlimSupportConf.USER);
    }
}

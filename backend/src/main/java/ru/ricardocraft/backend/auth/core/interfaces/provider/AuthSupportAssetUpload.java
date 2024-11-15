package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.Feature;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.base.events.request.AssetUploadInfoRequestEvent;
import ru.ricardocraft.backend.base.events.request.AuthRequestEvent;
import ru.ricardocraft.backend.base.events.request.GetAssetUploadUrlRequestEvent;

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

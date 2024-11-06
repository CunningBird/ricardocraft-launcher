package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.base.events.request.AssetUploadInfoRequestEvent;
import ru.ricardocraft.bff.base.events.request.AuthRequestEvent;
import ru.ricardocraft.bff.base.events.request.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.bff.auth.Feature;
import ru.ricardocraft.bff.auth.core.User;

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

package ru.ricardocraft.backend.auth.mix;

import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.User;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportAssetUpload;
import ru.ricardocraft.backend.base.events.request.AssetUploadInfoRequestEvent;

import java.util.Map;

public class UploadAssetMixProvider extends MixProvider implements AuthSupportAssetUpload {
    public Map<String, String> urls;
    public AssetUploadInfoRequestEvent.SlimSupportConf slimSupportConf;

    @Override
    public String getAssetUploadUrl(String name, User user) {
        return urls.get(name);
    }

    @Override
    public AssetUploadInfoRequestEvent getAssetUploadInfo(User user) {
        return new AssetUploadInfoRequestEvent(urls.keySet(), slimSupportConf);
    }

    @Override
    public void init(AuthCoreProvider core) {

    }

    @Override
    public void close() {

    }
}

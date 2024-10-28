package pro.gravit.launchserver.base.request.cabinet;

import pro.gravit.launchserver.base.events.request.AssetUploadInfoRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

package pro.gravit.launcher.gui.base.request.cabinet;

import pro.gravit.launcher.gui.base.events.request.AssetUploadInfoRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class AssetUploadInfoRequest extends Request<AssetUploadInfoRequestEvent> {
    @Override
    public String getType() {
        return "assetUploadInfo";
    }
}

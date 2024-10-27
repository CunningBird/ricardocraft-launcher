package pro.gravit.launcher.gui.base.request.management;

import pro.gravit.launcher.gui.base.events.request.FeaturesRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

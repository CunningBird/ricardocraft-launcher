package pro.gravit.launchserver.base.request.management;

import pro.gravit.launchserver.base.events.request.FeaturesRequestEvent;
import pro.gravit.launchserver.base.request.Request;

public class FeaturesRequest extends Request<FeaturesRequestEvent> {
    @Override
    public String getType() {
        return "features";
    }
}

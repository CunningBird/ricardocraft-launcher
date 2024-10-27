package pro.gravit.launcher.gui.base.request;

import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.WebSocketEvent;

public final class PingRequest extends Request<WebSocketEvent> {

    @Override
    public String getType() {
        return null;
    }
}

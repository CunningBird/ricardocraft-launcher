package pro.gravit.launcher.gui.base.request.websockets;

import pro.gravit.launcher.gui.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

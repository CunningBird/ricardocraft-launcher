package pro.gravit.launchserver.base.request.websockets;

import pro.gravit.launchserver.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

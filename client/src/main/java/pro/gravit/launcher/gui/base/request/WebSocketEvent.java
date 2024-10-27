package pro.gravit.launcher.gui.base.request;

import pro.gravit.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

package pro.gravit.launchserver.base.request;

import pro.gravit.launchserver.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

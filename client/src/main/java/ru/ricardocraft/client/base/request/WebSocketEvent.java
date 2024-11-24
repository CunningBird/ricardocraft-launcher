package ru.ricardocraft.client.base.request;

import ru.ricardocraft.client.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

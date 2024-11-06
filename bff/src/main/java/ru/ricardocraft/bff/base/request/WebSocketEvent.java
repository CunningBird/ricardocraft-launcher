package ru.ricardocraft.bff.base.request;

import ru.ricardocraft.bff.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

package ru.ricardocraft.backend.base.request;

import ru.ricardocraft.backend.base.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

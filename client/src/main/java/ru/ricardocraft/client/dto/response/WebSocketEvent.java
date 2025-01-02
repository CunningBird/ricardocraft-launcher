package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.base.utils.TypeSerializeInterface;

/**
 * The interface of all events sent by the server to the client
 */
public interface WebSocketEvent extends TypeSerializeInterface {
    String getType();
}

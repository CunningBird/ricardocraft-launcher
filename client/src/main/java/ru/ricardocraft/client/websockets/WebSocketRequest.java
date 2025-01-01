package ru.ricardocraft.client.websockets;

import ru.ricardocraft.client.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

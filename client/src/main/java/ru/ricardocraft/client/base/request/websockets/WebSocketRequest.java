package ru.ricardocraft.client.base.request.websockets;

import ru.ricardocraft.client.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

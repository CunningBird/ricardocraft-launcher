package ru.ricardocraft.client.dto.request.websockets;

import ru.ricardocraft.client.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

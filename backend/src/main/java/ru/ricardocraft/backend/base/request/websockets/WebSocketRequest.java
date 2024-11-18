package ru.ricardocraft.backend.base.request.websockets;

import ru.ricardocraft.backend.base.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

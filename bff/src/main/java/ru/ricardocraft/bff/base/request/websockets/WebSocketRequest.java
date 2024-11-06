package ru.ricardocraft.bff.base.request.websockets;

import ru.ricardocraft.bff.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

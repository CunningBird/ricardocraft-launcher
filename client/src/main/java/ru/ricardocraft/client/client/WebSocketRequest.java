package ru.ricardocraft.client.client;

import ru.ricardocraft.client.base.utils.TypeSerializeInterface;

public interface WebSocketRequest extends TypeSerializeInterface {
    String getType();
}

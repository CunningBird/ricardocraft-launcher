package ru.ricardocraft.backend.dto;

import ru.ricardocraft.backend.dto.response.TypeSerializeInterface;

import java.util.UUID;

/**
 * The class of all request events sent by the server to the client
 */
public abstract class AbstractResponse implements TypeSerializeInterface {
    public static final UUID eventUUID = UUID.fromString("fac0e2bd-9820-4449-b191-1d7c9bf781be");

    /**
     * UUID sent in request
     */
    public UUID requestUUID;
    public boolean closeChannel = false;
}

package ru.ricardocraft.bff.base.events;

import ru.ricardocraft.bff.base.request.WebSocketEvent;
import ru.ricardocraft.bff.core.LauncherNetworkAPI;

import java.util.UUID;

/**
 * The class of all request events sent by the server to the client
 */
public abstract class RequestEvent implements WebSocketEvent {
    public static final UUID eventUUID = UUID.fromString("fac0e2bd-9820-4449-b191-1d7c9bf781be");
    /**
     * UUID sent in request
     */
    @LauncherNetworkAPI
    public UUID requestUUID;
}

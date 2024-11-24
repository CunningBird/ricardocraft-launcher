package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.events.ClosePhase;

public class ClientExitPhase extends ClosePhase {
    public final int code;

    public ClientExitPhase(int code) {
        this.code = code;
    }
}

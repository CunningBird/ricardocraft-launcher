package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

public class ExitRequestEvent extends RequestEvent {
    public final ExitReason reason;

    public ExitRequestEvent(ExitReason reason) {
        this.reason = reason;
    }

    @Override
    public String getType() {
        return "exit";
    }

    public enum ExitReason {
        SERVER, CLIENT, TIMEOUT, NO_EXIT
    }
}

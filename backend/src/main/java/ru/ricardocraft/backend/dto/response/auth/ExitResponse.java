package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;

public class ExitResponse extends AbstractResponse {
    public final ExitReason reason;

    public ExitResponse(ExitReason reason) {
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

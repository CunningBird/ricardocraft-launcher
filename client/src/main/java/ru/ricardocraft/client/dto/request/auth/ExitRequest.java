package ru.ricardocraft.client.dto.request.auth;

import ru.ricardocraft.client.dto.response.ExitRequestEvent;
import ru.ricardocraft.client.dto.request.Request;

public class ExitRequest extends Request<ExitRequestEvent> {
    public final boolean exitAll;
    public final String username;

    public ExitRequest() {
        this.exitAll = false;
        this.username = null;
    }

    public ExitRequest(boolean exitAll) {
        this.exitAll = exitAll;
        this.username = null;
    }

    public ExitRequest(boolean exitAll, String username) {
        this.exitAll = exitAll;
        this.username = username;
    }

    @Override
    public String getType() {
        return "exit";
    }
}

package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;

import java.util.UUID;


public class ErrorRequestEvent extends RequestEvent {
    public static UUID uuid = UUID.fromString("0af22bc7-aa01-4881-bdbb-dc62b3cdac96");
    public final String error;

    public ErrorRequestEvent(String error) {
        this.error = error;
    }

    @Override
    public String getType() {
        return "error";
    }
}

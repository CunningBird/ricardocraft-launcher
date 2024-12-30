package ru.ricardocraft.backend.dto.response;

import ru.ricardocraft.backend.dto.AbstractResponse;

import java.util.UUID;


public class ErrorResponse extends AbstractResponse {
    public static UUID uuid = UUID.fromString("0af22bc7-aa01-4881-bdbb-dc62b3cdac96");
    public final String error;

    public ErrorResponse(String error) {
        this.error = error;
    }

    @Override
    public String getType() {
        return "error";
    }
}

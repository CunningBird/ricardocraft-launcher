package ru.ricardocraft.backend.socket.response.update;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class UpdateResponse extends SimpleResponse {
    public String dirName;

    @Override
    public String getType() {
        return "update";
    }
}

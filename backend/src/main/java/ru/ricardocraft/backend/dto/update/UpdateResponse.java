package ru.ricardocraft.backend.dto.update;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class UpdateResponse extends SimpleResponse {
    public String dirName;

    @Override
    public String getType() {
        return "update";
    }
}

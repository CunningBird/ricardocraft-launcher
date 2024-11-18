package ru.ricardocraft.backend.dto;

public class UnknownResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "unknown";
    }
}

package ru.ricardocraft.backend.socket.response;

public class UnknownResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "unknown";
    }
}

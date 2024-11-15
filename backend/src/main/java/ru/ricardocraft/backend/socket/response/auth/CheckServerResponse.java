package ru.ricardocraft.backend.socket.response.auth;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class CheckServerResponse extends SimpleResponse {

    public String serverID;
    public String username;
    public boolean needHardware;
    public boolean needProperties;

    @Override
    public String getType() {
        return "checkServer";
    }
}

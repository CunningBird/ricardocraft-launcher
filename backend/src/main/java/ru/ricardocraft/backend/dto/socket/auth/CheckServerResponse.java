package ru.ricardocraft.backend.dto.socket.auth;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class CheckServerResponse extends SimpleResponse {

    public String serverID;
    public String username;
    public boolean needHardware;
    public boolean needProperties;
}

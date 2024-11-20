package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class CheckServerResponse extends SimpleResponse {

    public String serverID;
    public String username;
    public boolean needHardware;
    public boolean needProperties;
}

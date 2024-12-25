package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class CheckServerResponse extends SimpleResponse {

    public String serverID;
    public String username;
    public boolean needHardware;
    public boolean needProperties;
}

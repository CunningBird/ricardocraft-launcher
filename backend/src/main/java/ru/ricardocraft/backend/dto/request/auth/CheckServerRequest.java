package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class CheckServerRequest extends AbstractRequest {

    public String serverID;
    public String username;
    public boolean needHardware;
    public boolean needProperties;
}

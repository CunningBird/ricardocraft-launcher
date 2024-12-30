package ru.ricardocraft.backend.dto.request.profile;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class ProfileByUsername extends AbstractRequest {
    public String username;
    public String client;
}

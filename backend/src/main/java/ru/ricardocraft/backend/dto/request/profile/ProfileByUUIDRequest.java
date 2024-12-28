package ru.ricardocraft.backend.dto.request.profile;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

import java.util.UUID;

public class ProfileByUUIDRequest extends AbstractRequest {
    public UUID uuid;
    public String client;
}

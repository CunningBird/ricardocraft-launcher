package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

import java.util.UUID;

public class AdditionalDataRequest extends AbstractRequest {
    public String username;
    public UUID uuid;
}

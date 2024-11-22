package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;

@Getter
@Setter
public class OpenIDProperties {
    private URI tokenUri;
    private String authorizationEndpoint;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private URI jwksUri;
    private String scopes;
    private String issuer;
    private ClaimExtractorProperties extractor;
}

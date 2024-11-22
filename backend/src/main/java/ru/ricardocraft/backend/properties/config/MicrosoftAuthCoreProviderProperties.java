package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MicrosoftAuthCoreProviderProperties {
    private String authCodeUrl = "https://login.live.com/oauth20_authorize.srf?client_id=%s&response_type=code&redirect_uri=%s&scope=XboxLive.signin offline_access";
    private String redirectUrl = "https://login.live.com/oauth20_desktop.srf";
    private String clientId = "00000000402b5328";
    private String clientSecret;
}

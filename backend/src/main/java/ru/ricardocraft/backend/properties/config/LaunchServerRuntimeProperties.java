package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// TODO if null whole block default, else multiModCheck current properties
public class LaunchServerRuntimeProperties {
    private String passwordEncryptKey; // not null, default - SecurityHelper.randomStringToken();
    private String runtimeEncryptKey; // default - SecurityHelper.randomStringAESKey()
    private String unlockSecret; // default - SecurityHelper.randomStringToken()
    private String registerApiKey; // default - SecurityHelper.randomStringToken();
    private String clientCheckSecret; // default - SecurityHelper.randomStringToken();
    private Long buildNumber; // default - 0
}

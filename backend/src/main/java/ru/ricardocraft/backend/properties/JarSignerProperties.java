package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JarSignerProperties {
    private Boolean enabled;
    private String keyStore;
    private String keyStoreType;
    private String keyStorePass;
    private String keyAlias;
    private String keyPass;
    private String metaInfKeyName;
    private String metaInfSfName;
    private String signAlgo;
    private Boolean checkCertificateExpired;
}

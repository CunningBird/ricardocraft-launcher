package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OSSLSignCodeProperties {
    private String timestampServer;
    private String osslsigncodePath;
    private List<String> customArgs = new ArrayList<>();
    private Boolean checkSignSize = true;
    private Boolean checkCorrectSign = true;
    private Boolean checkCorrectJar = true;
}

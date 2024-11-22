package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LauncherProperties {
    private Boolean compress;
    private Boolean stripLineNumbers;
    private Boolean deleteTempFiles;
    private Boolean certificatePinning;
    private Boolean encryptRuntime;
    private List<String> customJvmOptions;
    private Integer memoryLimit;
}

package ru.ricardocraft.backend.properties.httpserver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpServerBindAddressProperties {
    private String address;
    private Integer port;
}

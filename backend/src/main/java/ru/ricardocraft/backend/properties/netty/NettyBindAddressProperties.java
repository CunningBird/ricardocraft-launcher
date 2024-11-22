package ru.ricardocraft.backend.properties.netty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyBindAddressProperties {
    private String address;
    private Integer port;
}

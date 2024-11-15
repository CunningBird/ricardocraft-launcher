package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AuthLimiterProperties {
    private String message;
    private List<String> exclude;
    private Integer rateLimit;
    private Long rateLimitMillis;
}

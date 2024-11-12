package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.components.AbstractLimiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AuthLimiterProperties {
    private String message;
    private List<String> exclude;
    private Integer rateLimit;
    private Long rateLimitMillis;
}

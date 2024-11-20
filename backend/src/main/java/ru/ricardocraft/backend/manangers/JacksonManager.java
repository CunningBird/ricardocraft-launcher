package ru.ricardocraft.backend.manangers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JacksonManager {

    private transient final ObjectMapper mapper;

    public JacksonManager() {
        mapper = new ObjectMapper();
    }
}

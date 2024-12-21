package ru.ricardocraft.backend.manangers;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JacksonManager {

    private transient final ObjectMapper mapper;

    public JacksonManager() {
        mapper = new ObjectMapper();
        mapper.setBase64Variant(Base64Variants.MODIFIED_FOR_URL);
    }
}

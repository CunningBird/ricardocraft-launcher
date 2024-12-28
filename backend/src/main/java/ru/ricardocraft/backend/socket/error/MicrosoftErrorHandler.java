package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;

public class MicrosoftErrorHandler<T> implements HttpJsonErrorHandler<T, MicrosoftAuthCoreProvider.MicrosoftError> {
    private final Class<T> type;

    public MicrosoftErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, MicrosoftAuthCoreProvider.MicrosoftError> applyJson(JsonNode response, int statusCode, ObjectMapper objectMapper) throws JsonProcessingException {
        if (statusCode < 200 || statusCode >= 300) {
            return new HttpOptional<>(null, objectMapper.readValue(response.asText(), MicrosoftAuthCoreProvider.MicrosoftError.class), statusCode);
        } else {
            return new HttpOptional<>(objectMapper.readValue(response.asText(), type), null, statusCode);
        }
    }
}
package ru.ricardocraft.backend.socket.handlers.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ru.ricardocraft.backend.manangers.JacksonManager;

public class SimpleErrorHandler<T> implements HttpJsonErrorHandler<T, SimpleError> {
    private final Class<T> type;

    public SimpleErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, SimpleError> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
        if (statusCode < 200 || statusCode >= 300) {
            return new HttpOptional<>(null, jacksonManager.getMapper().readValue(response.asText(), SimpleError.class), statusCode);
        }
        if (type == Void.class) {
            return new HttpOptional<>(null, null, statusCode);
        }
        return new HttpOptional<>(jacksonManager.getMapper().readValue(response.toString(), type), null, statusCode);
    }
}
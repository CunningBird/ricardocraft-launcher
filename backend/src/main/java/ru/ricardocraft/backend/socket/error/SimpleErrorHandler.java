package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SimpleErrorHandler<T> implements HttpJsonErrorHandler<T, SimpleError> {
    private final Class<T> type;

    public SimpleErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, SimpleError> applyJson(JsonNode response, int statusCode, ObjectMapper objectMapper) throws JsonProcessingException {
        if (statusCode < 200 || statusCode >= 300) {
            return new HttpOptional<>(null, objectMapper.readValue(response.asText(), SimpleError.class), statusCode);
        }
        if (type == Void.class) {
            return new HttpOptional<>(null, null, statusCode);
        }
        return new HttpOptional<>(objectMapper.readValue(response.toString(), type), null, statusCode);
    }
}
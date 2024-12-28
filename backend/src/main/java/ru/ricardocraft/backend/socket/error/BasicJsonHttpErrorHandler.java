package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class BasicJsonHttpErrorHandler<T> implements HttpJsonErrorHandler<T, Void> {
    private final Class<T> type;

    public BasicJsonHttpErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, Void> applyJson(JsonNode response, int statusCode, ObjectMapper objectMapper) throws JsonProcessingException {
        return new HttpOptional<>(objectMapper.readValue(response.asText(), type), null, statusCode);
    }
}
package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ru.ricardocraft.backend.manangers.JacksonManager;

public final class BasicJsonHttpErrorHandler<T> implements HttpJsonErrorHandler<T, Void> {
    private final Class<T> type;

    public BasicJsonHttpErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, Void> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
        return new HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
    }
}
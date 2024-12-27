package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;
import ru.ricardocraft.backend.manangers.JacksonManager;

public class MicrosoftErrorHandler<T> implements HttpJsonErrorHandler<T, MicrosoftAuthCoreProvider.MicrosoftError> {
    private final Class<T> type;

    public MicrosoftErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, MicrosoftAuthCoreProvider.MicrosoftError> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
        if (statusCode < 200 || statusCode >= 300) {
            return new HttpOptional<>(null, jacksonManager.getMapper().readValue(response.asText(), MicrosoftAuthCoreProvider.MicrosoftError.class), statusCode);
        } else {
            return new HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
        }
    }
}
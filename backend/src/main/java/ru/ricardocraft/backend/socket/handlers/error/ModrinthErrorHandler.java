package ru.ricardocraft.backend.socket.handlers.error;

import com.fasterxml.jackson.databind.JsonNode;
import ru.ricardocraft.backend.manangers.JacksonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpResponse;

public class ModrinthErrorHandler<T> implements HttpErrorHandler<T, Void> {
    private final Class<T> type;

    public ModrinthErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, Void> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return new HttpOptional<>(null, null, response.statusCode());
        }
        try (Reader reader = new InputStreamReader(response.body())) {
            JsonNode element = jacksonManager.getMapper().readTree(reader);
            return new HttpOptional<>(jacksonManager.getMapper().readValue(element.asText(), type), null, response.statusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
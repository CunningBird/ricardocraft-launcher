package ru.ricardocraft.backend.client.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpResponse;

public class CurseForgeErrorHandler<T> implements HttpErrorHandler<T, Void> {
    private final Class<T> type;

    public CurseForgeErrorHandler(Class<T> type) {
        this.type = type;
    }

    @Override
    public HttpOptional<T, Void> apply(HttpResponse<InputStream> response, ObjectMapper objectMapper) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return new HttpOptional<>(null, null, response.statusCode());
        }
        try (Reader reader = new InputStreamReader(response.body())) {
            JsonNode element = objectMapper.readTree(reader);
            return new HttpOptional<>(objectMapper.readValue(element.get("data").asText(), type), null, response.statusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
package ru.ricardocraft.backend.client.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpResponse;

public interface HttpJsonErrorHandler<T, E> extends HttpErrorHandler<T, E> {
    HttpOptional<T, E> applyJson(JsonNode response, int statusCode, ObjectMapper objectMapper) throws JsonProcessingException;

    default HttpOptional<T, E> apply(HttpResponse<InputStream> response, ObjectMapper objectMapper) {
        try (Reader reader = new InputStreamReader(response.body())) {
            var element = objectMapper.readTree(reader);
            return applyJson(element, response.statusCode(), objectMapper);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
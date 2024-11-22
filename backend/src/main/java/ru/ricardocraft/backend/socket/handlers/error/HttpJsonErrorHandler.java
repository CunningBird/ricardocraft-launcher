package ru.ricardocraft.backend.socket.handlers.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ru.ricardocraft.backend.manangers.JacksonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpResponse;

public interface HttpJsonErrorHandler<T, E> extends HttpErrorHandler<T, E> {
    HttpOptional<T, E> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException;

    default HttpOptional<T, E> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager) {
        try (Reader reader = new InputStreamReader(response.body())) {
            var element = jacksonManager.getMapper().readTree(reader);
            return applyJson(element, response.statusCode(), jacksonManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
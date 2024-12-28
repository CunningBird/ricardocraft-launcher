package ru.ricardocraft.backend.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.socket.error.HttpErrorHandler;
import ru.ricardocraft.backend.socket.error.HttpOptional;
import ru.ricardocraft.backend.socket.error.SimpleError;
import ru.ricardocraft.backend.socket.error.SimpleErrorHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class HttpRequester {

    private final HttpClient client = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper;

    @Autowired
    public HttpRequester(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HttpRequest get(String url, String token) {
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .uri(new URI(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofMillis(10000));
            if (token != null) {
                requestBuilder.header("Authorization", "Bearer ".concat(token));
            }
            return requestBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> HttpOptional<T, SimpleError> send(HttpRequest request, Class<T> clazz) throws IOException {
        return send(request, new SimpleErrorHandler<>(clazz));
    }

    public <T, E> HttpOptional<T, E> send(HttpRequest request, HttpErrorHandler<T, E> handler) throws IOException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return handler.apply(response, objectMapper);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}

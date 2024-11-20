package ru.ricardocraft.backend;

import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.manangers.GsonManager;
import ru.ricardocraft.backend.socket.HttpSender;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

@Component
public class HttpRequester {
    private transient final HttpClient httpClient = HttpClient.newBuilder().build();
    private transient final HttpSender httpSender;

    @Autowired
    public HttpRequester(HttpSender httpSender) {
        this.httpSender = httpSender;
    }

    public <T> HttpSender.SimpleErrorHandler<T> makeEH(Class<T> clazz) {
        return new HttpSender.SimpleErrorHandler<>(clazz);
    }

    public <T> HttpSender.SimpleErrorHandler<T> makeEH(Type clazz) {
        return new HttpSender.SimpleErrorHandler<>(clazz);
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

    public <T> HttpSender.HttpOptional<T, SimpleError> send(HttpRequest request, Class<T> clazz) throws IOException {
        return httpSender.send(httpClient, request, makeEH(clazz));
    }

    public <T> HttpSender.HttpOptional<T, SimpleError> send(HttpRequest request, Type type) throws IOException {
        return httpSender.send(httpClient, request, makeEH(type));
    }

    public static class SimpleError {
        public String error;
        public int code;

        public SimpleError(String error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return "SimpleError{" +
                    "error='" + error + '\'' +
                    ", code=" + code +
                    '}';
        }
    }
}

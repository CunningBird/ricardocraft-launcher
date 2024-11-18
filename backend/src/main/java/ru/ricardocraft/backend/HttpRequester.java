package ru.ricardocraft.backend;

import com.google.gson.JsonElement;
import ru.ricardocraft.backend.base.helper.HttpHelper;
import ru.ricardocraft.backend.manangers.GsonManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

public class HttpRequester {
    private transient final HttpClient httpClient = HttpClient.newBuilder().build();
    private transient final GsonManager gsonManager;

    public HttpRequester(GsonManager gsonManager) {
        this.gsonManager = gsonManager;
    }

    public <T> SimpleErrorHandler<T> makeEH(Class<T> clazz) {
        return new SimpleErrorHandler<>(clazz, gsonManager);
    }

    public <T> SimpleErrorHandler<T> makeEH(Type clazz) {
        return new SimpleErrorHandler<>(clazz, gsonManager);
    }

    public <T> HttpRequest get(String url, String token) {
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

    public <T> HttpRequest post(String url, T request, String token) {
        try {
            var requestBuilder = HttpRequest.newBuilder()
                    .method("POST", HttpRequest.BodyPublishers.ofString(gsonManager.gson.toJson(request)))
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

    public <T> HttpHelper.HttpOptional<T, SimpleError> send(HttpRequest request, Class<T> clazz) throws IOException {
        return HttpHelper.send(httpClient, request, makeEH(clazz), gsonManager);
    }

    public <T> HttpHelper.HttpOptional<T, SimpleError> send(HttpRequest request, Type type) throws IOException {
        return HttpHelper.send(httpClient, request, makeEH(type), gsonManager);
    }


    public static class SimpleErrorHandler<T> implements HttpHelper.HttpJsonErrorHandler<T, SimpleError> {
        private final Type type;
        private final GsonManager gsonManager;

        private SimpleErrorHandler(Type type, GsonManager gsonManager) {
            this.type = type;
            this.gsonManager = gsonManager;
        }

        @Override
        public HttpHelper.HttpOptional<T, SimpleError> applyJson(JsonElement response, int statusCode) {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpHelper.HttpOptional<>(null, gsonManager.gson.fromJson(response, SimpleError.class), statusCode);
            }
            if (type == Void.class) {
                return new HttpHelper.HttpOptional<>(null, null, statusCode);
            }
            return new HttpHelper.HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
        }
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

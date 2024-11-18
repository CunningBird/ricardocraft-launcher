package ru.ricardocraft.backend.base.helper;

import com.google.gson.JsonElement;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.manangers.GsonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class HttpHelper {

    private HttpHelper() {
        throw new UnsupportedOperationException();
    }

    public static <T, E> HttpOptional<T, E> send(HttpClient client,
                                                 HttpRequest request,
                                                 HttpErrorHandler<T, E> handler,
                                                 GsonManager gsonManager) throws IOException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return handler.apply(response, gsonManager);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public interface HttpErrorHandler<T, E> {
        HttpOptional<T, E> apply(HttpResponse<InputStream> response, GsonManager gsonManager);
    }

    public interface HttpJsonErrorHandler<T, E> extends HttpErrorHandler<T, E> {
        HttpOptional<T, E> applyJson(JsonElement response, int statusCode);

        default HttpOptional<T, E> apply(HttpResponse<InputStream> response, GsonManager gsonManager) {
            try (Reader reader = new InputStreamReader(response.body())) {
                var element = gsonManager.gson.fromJson(reader, JsonElement.class);
                return applyJson(element, response.statusCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class HttpOptional<T, E> {
        protected final T result;
        protected final E error;
        protected final int statusCode;

        public HttpOptional(T result, E error, int statusCode) {
            this.result = result;
            this.error = error;
            this.statusCode = statusCode;
        }

        public T result() {
            return result;
        }

        public E error() {
            return error;
        }

        public int statusCode() {
            return statusCode;
        }

        public boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }

        public T getOrThrow() throws RequestException {
            if (isSuccessful()) {
                return result;
            } else {
                throw new RequestException(error == null ? "statusCode %d".formatted(statusCode) : error.toString());
            }
        }
    }

    public static final class BasicJsonHttpErrorHandler<T> implements HttpJsonErrorHandler<T, Void> {
        private final Class<T> type;
        private final GsonManager gsonManager;

        public BasicJsonHttpErrorHandler(Class<T> type, GsonManager gsonManager) {
            this.type = type;
            this.gsonManager = gsonManager;
        }

        @Override
        public HttpOptional<T, Void> applyJson(JsonElement response, int statusCode) {
            return new HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
        }
    }

}

package ru.ricardocraft.backend.socket;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.HttpRequester;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.manangers.GsonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
public class HttpSender {

    public final GsonManager gsonManager;

    public <T, E> HttpOptional<T, E> send(HttpClient client,
                                          HttpRequest request,
                                          HttpErrorHandler<T, E> handler) throws IOException {
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
        HttpOptional<T, E> applyJson(JsonElement response, int statusCode, GsonManager gsonManager);

        default HttpOptional<T, E> apply(HttpResponse<InputStream> response, GsonManager gsonManager) {
            try (Reader reader = new InputStreamReader(response.body())) {
                var element = gsonManager.gson.fromJson(reader, JsonElement.class);
                return applyJson(element, response.statusCode(), gsonManager);
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

        public BasicJsonHttpErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpOptional<T, Void> applyJson(JsonElement response, int statusCode, GsonManager gsonManager) {
            return new HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
        }
    }

    public static class ModrinthErrorHandler<T> implements HttpSender.HttpErrorHandler<T, Void> {
        private final Type type;

        public ModrinthErrorHandler(Type type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, Void> apply(HttpResponse<InputStream> response, GsonManager gsonManager) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new HttpSender.HttpOptional<>(null, null, response.statusCode());
            }
            try (Reader reader = new InputStreamReader(response.body())) {
                JsonElement element = gsonManager.gson.fromJson(reader, JsonElement.class);
                return new HttpSender.HttpOptional<>(gsonManager.gson.fromJson(element, type), null, response.statusCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class CurseForgeErrorHandler<T> implements HttpSender.HttpErrorHandler<T, Void> {
        private final Class<T> type;

        public CurseForgeErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, Void> apply(HttpResponse<InputStream> response, GsonManager gsonManager) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new HttpSender.HttpOptional<>(null, null, response.statusCode());
            }
            try (Reader reader = new InputStreamReader(response.body())) {
                JsonElement element = gsonManager.gson.fromJson(reader, JsonElement.class);
                return new HttpSender.HttpOptional<>(gsonManager.gson.fromJson(element.getAsJsonObject().get("data"), type), null, response.statusCode());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class XSTSErrorHandler<T> implements HttpSender.HttpJsonErrorHandler<T, MicrosoftAuthCoreProvider.XSTSError> {
        private final Class<T> type;

        public XSTSErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, MicrosoftAuthCoreProvider.XSTSError> applyJson(JsonElement response, int statusCode, GsonManager gsonManager) {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, gsonManager.gson.fromJson(response, MicrosoftAuthCoreProvider.XSTSError.class), statusCode);
            } else {
                return new HttpSender.HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
            }
        }
    }

    public static class MicrosoftErrorHandler<T> implements HttpSender.HttpJsonErrorHandler<T, MicrosoftAuthCoreProvider.MicrosoftError> {
        private final Class<T> type;

        public MicrosoftErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, MicrosoftAuthCoreProvider.MicrosoftError> applyJson(JsonElement response, int statusCode, GsonManager gsonManager) {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, gsonManager.gson.fromJson(response, MicrosoftAuthCoreProvider.MicrosoftError.class), statusCode);
            } else {
                return new HttpSender.HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
            }
        }
    }

    public static class SimpleErrorHandler<T> implements HttpSender.HttpJsonErrorHandler<T, HttpRequester.SimpleError> {
        private final Type type;

        public SimpleErrorHandler(Type type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, HttpRequester.SimpleError> applyJson(JsonElement response, int statusCode, GsonManager gsonManager) {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, gsonManager.gson.fromJson(response, HttpRequester.SimpleError.class), statusCode);
            }
            if (type == Void.class) {
                return new HttpSender.HttpOptional<>(null, null, statusCode);
            }
            return new HttpSender.HttpOptional<>(gsonManager.gson.fromJson(response, type), null, statusCode);
        }
    }
}

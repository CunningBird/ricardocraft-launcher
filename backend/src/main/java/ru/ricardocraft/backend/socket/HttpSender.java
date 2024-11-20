package ru.ricardocraft.backend.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.manangers.JacksonManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
public class HttpSender {

    public final JacksonManager jacksonManager;

    private final HttpClient client = HttpClient.newBuilder().build();

    public <T, E> HttpOptional<T, E> send(HttpRequest request, HttpErrorHandler<T, E> handler) throws IOException {
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            return handler.apply(response, jacksonManager);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public interface HttpErrorHandler<T, E> {
        HttpOptional<T, E> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager);
    }

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
        public HttpOptional<T, Void> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
            return new HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
        }
    }

    public static class ModrinthErrorHandler<T> implements HttpSender.HttpErrorHandler<T, Void> {
        private final Class<T> type;

        public ModrinthErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, Void> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new HttpSender.HttpOptional<>(null, null, response.statusCode());
            }
            try (Reader reader = new InputStreamReader(response.body())) {
                JsonNode element = jacksonManager.getMapper().readTree(reader);
                return new HttpSender.HttpOptional<>(jacksonManager.getMapper().readValue(element.asText(), type), null, response.statusCode());
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
        public HttpSender.HttpOptional<T, Void> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager) {
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new HttpSender.HttpOptional<>(null, null, response.statusCode());
            }
            try (Reader reader = new InputStreamReader(response.body())) {
                JsonNode element = jacksonManager.getMapper().readTree(reader);
                return new HttpSender.HttpOptional<>(jacksonManager.getMapper().readValue(element.get("data").asText(), type), null, response.statusCode());
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
        public HttpSender.HttpOptional<T, MicrosoftAuthCoreProvider.XSTSError> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, jacksonManager.getMapper().readValue(response.asText(), MicrosoftAuthCoreProvider.XSTSError.class), statusCode);
            } else {
                return new HttpSender.HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
            }
        }
    }

    public static class MicrosoftErrorHandler<T> implements HttpSender.HttpJsonErrorHandler<T, MicrosoftAuthCoreProvider.MicrosoftError> {
        private final Class<T> type;

        public MicrosoftErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, MicrosoftAuthCoreProvider.MicrosoftError> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, jacksonManager.getMapper().readValue(response.asText(), MicrosoftAuthCoreProvider.MicrosoftError.class), statusCode);
            } else {
                return new HttpSender.HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
            }
        }
    }

    public static class SimpleErrorHandler<T> implements HttpSender.HttpJsonErrorHandler<T, HttpRequester.SimpleError> {
        private final Class<T> type;

        public SimpleErrorHandler(Class<T> type) {
            this.type = type;
        }

        @Override
        public HttpSender.HttpOptional<T, HttpRequester.SimpleError> applyJson(JsonNode response, int statusCode, JacksonManager jacksonManager) throws JsonProcessingException {
            if (statusCode < 200 || statusCode >= 300) {
                return new HttpSender.HttpOptional<>(null, jacksonManager.getMapper().readValue(response.asText(), HttpRequester.SimpleError.class), statusCode);
            }
            if (type == Void.class) {
                return new HttpSender.HttpOptional<>(null, null, statusCode);
            }
            return new HttpSender.HttpOptional<>(jacksonManager.getMapper().readValue(response.asText(), type), null, statusCode);
        }
    }
}

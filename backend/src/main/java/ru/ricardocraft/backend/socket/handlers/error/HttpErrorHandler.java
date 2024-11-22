package ru.ricardocraft.backend.socket.handlers.error;

import ru.ricardocraft.backend.manangers.JacksonManager;

import java.io.InputStream;
import java.net.http.HttpResponse;

public interface HttpErrorHandler<T, E> {
    HttpOptional<T, E> apply(HttpResponse<InputStream> response, JacksonManager jacksonManager);
}
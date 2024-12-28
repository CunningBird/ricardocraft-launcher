package ru.ricardocraft.backend.socket.error;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.http.HttpResponse;

public interface HttpErrorHandler<T, E> {
    HttpOptional<T, E> apply(HttpResponse<InputStream> response, ObjectMapper objectMapper);
}
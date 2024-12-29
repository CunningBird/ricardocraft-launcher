package ru.ricardocraft.backend.client.error;

import ru.ricardocraft.backend.dto.request.RequestException;

public class HttpOptional<T, E> {
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

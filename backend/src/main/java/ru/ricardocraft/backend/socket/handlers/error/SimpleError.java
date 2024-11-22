package ru.ricardocraft.backend.socket.handlers.error;

public class SimpleError {
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
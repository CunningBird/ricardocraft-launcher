package ru.ricardocraft.backend.dto;

import java.util.UUID;

public abstract class SimpleResponse {
    public UUID requestUUID;
    public transient UUID connectUUID;
    public transient String ip;

    abstract public String getType();

    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ;
    }

    public enum ThreadSafeStatus {
        NONE, READ, READ_WRITE
    }
}

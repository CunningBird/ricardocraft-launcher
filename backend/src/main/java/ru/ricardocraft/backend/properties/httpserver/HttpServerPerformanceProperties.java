package ru.ricardocraft.backend.properties.httpserver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpServerPerformanceProperties {
    private Boolean usingEpoll;
    private Integer bossThread;
    private Integer workerThread;
    private Integer schedulerThread;
    private Integer maxWebSocketRequestBytes;
    private Boolean disableThreadSafeClientObject;
    private HttpServerExecutorType executorType;
}

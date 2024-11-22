package ru.ricardocraft.backend.properties.netty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyPerformanceProperties {
    private Boolean usingEpoll;
    private Integer bossThread;
    private Integer workerThread;
    private Integer schedulerThread;
    private Integer maxWebSocketRequestBytes;
    private Boolean disableThreadSafeClientObject;
    private NettyExecutorType executorType;
}

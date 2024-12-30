package ru.ricardocraft.backend.service.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.JVMHelper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerStatusService {

    public void serverStatus() {
        log.info("Show server status");
        log.info("Memory: free {} | total: {} | max: {}", JVMHelper.RUNTIME.freeMemory(), JVMHelper.RUNTIME.totalMemory(), JVMHelper.RUNTIME.maxMemory());
        long uptime = JVMHelper.RUNTIME_MXBEAN.getUptime() / 1000;
        long second = uptime % 60;
        long min = (uptime / 60) % 60;
        long hour = (uptime / 60 / 60) % 24;
        long days = (uptime / 60 / 60 / 24);
        log.info("Uptime: {} days {} hours {} minutes {} seconds", days, hour, min, second);
        log.info("Uptime (double): {}", (double) JVMHelper.RUNTIME_MXBEAN.getUptime() / 1000);
    }
}

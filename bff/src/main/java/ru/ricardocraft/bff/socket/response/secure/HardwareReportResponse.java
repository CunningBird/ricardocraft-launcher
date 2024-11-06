package ru.ricardocraft.bff.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.HardwareReportRequestEvent;
import ru.ricardocraft.bff.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.bff.auth.protect.interfaces.HardwareProtectHandler;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

public class HardwareReportResponse extends SimpleResponse {
    public HardwareReportRequest.HardwareInfo hardware;

    @Override
    public String getType() {
        return "hardwareReport";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (client.trustLevel == null || client.trustLevel.publicKey == null) {
            sendError("Invalid request");
            return;
        }
        if (server.config.protectHandler instanceof HardwareProtectHandler hardwareProtectHandler) {
            try {
                hardwareProtectHandler.onHardwareReport(this, client);
            } catch (SecurityException e) {
                sendError(e.getMessage());
            }
        } else {
            sendResult(new HardwareReportRequestEvent());
        }
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}

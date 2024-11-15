package ru.ricardocraft.backend.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.protect.interfaces.HardwareProtectHandler;
import ru.ricardocraft.backend.base.events.request.HardwareReportRequestEvent;
import ru.ricardocraft.backend.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

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
        if (protectHandler instanceof HardwareProtectHandler hardwareProtectHandler) {
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

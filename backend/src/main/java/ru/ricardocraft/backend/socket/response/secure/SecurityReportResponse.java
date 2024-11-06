package ru.ricardocraft.backend.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.base.events.request.SecurityReportRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class SecurityReportResponse extends SimpleResponse {
    public String reportType;
    public String smallData;
    public String largeData;
    public byte[] smallBytes;
    public byte[] largeBytes;

    @Override
    public String getType() {
        return "securityReport";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!(server.config.protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            sendError("Method not allowed");
        } else {
            SecurityReportRequestEvent event = secureProtectHandler.onSecurityReport(this, client);
            sendResult(event);
        }
    }
}

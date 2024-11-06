package ru.ricardocraft.bff.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.bff.base.events.request.SecurityReportRequestEvent;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

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

package ru.ricardocraft.client.client;

import ru.ricardocraft.client.base.events.ExtendedTokenRequestEvent;
import ru.ricardocraft.client.base.events.NotificationEvent;
import ru.ricardocraft.client.base.events.request.SecurityReportRequestEvent;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.WebSocketEvent;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class BasicLauncherEventHandler implements RequestService.EventHandler {

    @Override
    public <T extends WebSocketEvent> boolean eventHandle(T event) {
        if (event instanceof SecurityReportRequestEvent event1) {
            if (event1.action == SecurityReportRequestEvent.ReportAction.TOKEN_EXPIRED) {
                try {
                    Request.restore();
                } catch (Exception e) {
                    LogHelper.error(e);
                }
            }
        } else if (event instanceof ExtendedTokenRequestEvent event1) {
            String token = event1.getExtendedToken();
            if (token != null) {
                Request.addExtendedToken(event1.getExtendedTokenName(), new Request.ExtendedToken(event1.getExtendedToken(), event1.getExtendedTokenExpire()));
            }
        } else if (event instanceof NotificationEvent n) {
            if (DialogService.isNotificationsAvailable()) {
                DialogService.createNotification(n.icon, n.head, n.message);
            }
        }
        return false;
    }
}

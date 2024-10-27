package pro.gravit.launcher.gui.client;

import pro.gravit.launcher.gui.base.events.ExtendedTokenRequestEvent;
import pro.gravit.launcher.gui.base.events.NotificationEvent;
import pro.gravit.launcher.gui.base.events.request.SecurityReportRequestEvent;
import pro.gravit.launcher.gui.base.request.Request;
import pro.gravit.launcher.gui.base.request.RequestService;
import pro.gravit.launcher.gui.base.request.WebSocketEvent;
import pro.gravit.launcher.gui.client.api.DialogService;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

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

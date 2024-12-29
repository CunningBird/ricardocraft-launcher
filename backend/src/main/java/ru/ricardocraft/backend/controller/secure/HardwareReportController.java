package ru.ricardocraft.backend.controller.secure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.service.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.service.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
public class HardwareReportController extends AbstractController {

    private final HttpServerProperties httpServerProperties;
    private final ProtectHandler protectHandler;

    @Autowired
    public HardwareReportController(ServerWebSocketHandler handler,
                                    HttpServerProperties httpServerProperties,
                                    ProtectHandler protectHandler) {
        super(HardwareReportRequest.class, handler);
        this.httpServerProperties = httpServerProperties;
        this.protectHandler = protectHandler;
    }

    @Override
    public HardwareReportResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        HardwareReportRequest response = (HardwareReportRequest) rawResponse;

        if (client.trustLevel == null || client.trustLevel.publicKey == null) {
            throw new Exception("Invalid request");
        }
        if (protectHandler instanceof AdvancedProtectHandler advancedProtectHandler) {
            try {
                if (!client.isAuth) {
                    throw new Exception("Access denied");
                }
                if (client.trustLevel.hardwareInfo != null) {
                    return new HardwareReportResponse(
                            advancedProtectHandler.createHardwareToken(client.username, client.trustLevel.hardwareInfo),
                            SECONDS.toMillis(httpServerProperties.getSecurity().getHardwareTokenExpire())
                    );
                }
                log.debug("HardwareInfo received");
                {
                    var authSupportHardware = client.auth.isSupport(AuthSupportHardware.class);
                    if (authSupportHardware != null) {
                        UserHardware hardware = authSupportHardware.getHardwareInfoByData(response.hardware);
                        if (hardware == null) {
                            hardware = authSupportHardware.createHardwareInfo(response.hardware, client.trustLevel.publicKey);
                        } else {
                            authSupportHardware.addPublicKeyToHardwareInfo(hardware, client.trustLevel.publicKey);
                        }
                        authSupportHardware.connectUserAndHardware(client.sessionObject, hardware);
                        if (hardware.isBanned()) {
                            throw new SecurityException("Your hardware banned");
                        }
                        client.trustLevel.hardwareInfo = hardware;
                        return new HardwareReportResponse(
                                advancedProtectHandler.createHardwareToken(client.username, hardware),
                                SECONDS.toMillis(httpServerProperties.getSecurity().getHardwareTokenExpire())
                        );
                    } else {
                        log.error("AuthCoreProvider not supported hardware");
                        throw new Exception("AuthCoreProvider not supported hardware");
                    }
                }
            } catch (SecurityException e) {
                throw new Exception(e.getMessage());
            }
        } else {
            return new HardwareReportResponse();
        }
    }
}

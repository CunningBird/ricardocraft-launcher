package ru.ricardocraft.backend.service.controller.secure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.service.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.service.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class HardwareReportService {

    private final HttpServerProperties httpServerProperties;
    private final ProtectHandler protectHandler;

    public HardwareReportResponse hardwareReport(HardwareReportRequest request, Client client) throws Exception {
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
                        UserHardware hardware = authSupportHardware.getHardwareInfoByData(request.hardware);
                        if (hardware == null) {
                            hardware = authSupportHardware.createHardwareInfo(request.hardware, client.trustLevel.publicKey);
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

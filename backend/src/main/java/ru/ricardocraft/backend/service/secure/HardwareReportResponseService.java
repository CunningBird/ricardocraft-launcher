package ru.ricardocraft.backend.service.secure;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.dto.events.request.secure.HardwareReportRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class HardwareReportResponseService extends AbstractResponseService {

    private static final Logger logger = LogManager.getLogger(HardwareReportResponseService.class);

    private final NettyProperties nettyProperties;
    private final ProtectHandler protectHandler;

    @Autowired
    public HardwareReportResponseService(WebSocketService service,
                                         NettyProperties nettyProperties,
                                         ProtectHandler protectHandler) {
        super(HardwareReportResponse.class, service);
        this.nettyProperties = nettyProperties;
        this.protectHandler = protectHandler;
    }

    @Override
    public HardwareReportRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        HardwareReportResponse response = (HardwareReportResponse) rawResponse;

        if (client.trustLevel == null || client.trustLevel.publicKey == null) {
            throw new Exception("Invalid request");
        }
        if (protectHandler instanceof AdvancedProtectHandler advancedProtectHandler) {
            try {
                if (!client.isAuth) {
                    throw new Exception("Access denied");
                }
                if (client.trustLevel.hardwareInfo != null) {
                    return new HardwareReportRequestEvent(
                            advancedProtectHandler.createHardwareToken(client.username, client.trustLevel.hardwareInfo),
                            SECONDS.toMillis(nettyProperties.getSecurity().getHardwareTokenExpire())
                    );
                }
                logger.debug("HardwareInfo received");
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
                        return new HardwareReportRequestEvent(
                                advancedProtectHandler.createHardwareToken(client.username, hardware),
                                SECONDS.toMillis(nettyProperties.getSecurity().getHardwareTokenExpire())
                        );
                    } else {
                        logger.error("AuthCoreProvider not supported hardware");
                        throw new Exception("AuthCoreProvider not supported hardware");
                    }
                }
            } catch (SecurityException e) {
                throw new Exception(e.getMessage());
            }
        } else {
            return new HardwareReportRequestEvent();
        }
    }
}

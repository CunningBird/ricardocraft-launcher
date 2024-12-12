package ru.ricardocraft.client.launch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.client.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.client.base.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.client.runtime.utils.HWIDProvider;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.utils.helper.CommonHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper;

import java.io.IOException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

@Component
public class RuntimeSecurityService {

    private final ECPublicKey publicKey;
    private final ECPrivateKey privateKey;

    private final RequestService requestService;
    private final LaunchService launchService;
    private final Boolean[] waitObject = new Boolean[]{null};

    @Autowired
    public RuntimeSecurityService(ECPublicKey publicKey,
                                  ECPrivateKey privateKey,
                                  RequestService requestService,
                                  LaunchService launchService) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.requestService = requestService;
        this.launchService = launchService;
    }

    public void startRequest() throws IOException {
        requestService.request(new GetSecureLevelInfoRequest()).thenAccept((event) -> {
            if (!event.enabled || event.verifySecureKey == null) {
                LogHelper.info("Advanced security level disabled");
                notifyWaitObject(false);
                return;
            }
            byte[] signature = sign(event.verifySecureKey);
            try {
                requestService.request(
                                new VerifySecureLevelKeyRequest(publicKey.getEncoded(), signature))
                        .thenAccept((event1) -> {
                            if (!event1.needHardwareInfo) {
                                simpleGetHardwareToken();
                            } else {
                                doCollectHardwareInfo(!event1.onlyStatisticInfo);
                            }
                        }).exceptionally((e) -> {
                            launchService.createNotification("Hardware Checker", e.getCause().getMessage());
                            notifyWaitObject(false);
                            return null;
                        });
            } catch (IOException e) {
                LogHelper.error("VerifySecureLevel failed: %s", e.getMessage());
                notifyWaitObject(false);
            }
        }).exceptionally((e) -> {
            LogHelper.info("Advanced security level disabled(exception)");
            notifyWaitObject(false);
            return null;
        });
    }

    private void simpleGetHardwareToken() {
        try {
            requestService.request(new HardwareReportRequest()).thenAccept((response) -> {
                LogHelper.info("Advanced security level success completed");
                notifyWaitObject(true);
            }).exceptionally((e) -> {
                launchService.createNotification("Hardware Checker", e.getCause().getMessage());
                notifyWaitObject(false);
                return null;
            });
        } catch (IOException e) {
            launchService.createNotification("Hardware Checker", e.getCause().getMessage());
            notifyWaitObject(false);
        }
    }

    private void doCollectHardwareInfo(boolean needSerial) {
        CommonHelper.newThread("HardwareInfo Collector Thread", true, () -> {
            try {
                HWIDProvider provider = new HWIDProvider();
                HardwareReportRequest.HardwareInfo info = provider.getHardwareInfo(needSerial);
                HardwareReportRequest reportRequest = new HardwareReportRequest();
                reportRequest.hardware = info;
                requestService.request(reportRequest).thenAccept((event) -> {
                    LogHelper.info("Advanced security level success completed");
                    notifyWaitObject(true);
                }).exceptionally((exc) -> {
                    launchService.createNotification("Hardware Checker", exc.getCause().getMessage());
                    return null;
                });
            } catch (Throwable e) {
                LogHelper.error(e);
                notifyWaitObject(false);
            }
        }).start();
    }

    private void notifyWaitObject(boolean state) {
        synchronized (waitObject) {
            waitObject[0] = state;
            waitObject.notifyAll();
        }
    }

    public byte[] sign(byte[] data) {
        return SecurityHelper.sign(data, privateKey);
    }
}
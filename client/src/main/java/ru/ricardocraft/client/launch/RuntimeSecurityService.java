package ru.ricardocraft.client.launch;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.client.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.client.base.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.client.impl.MessageManager;
import ru.ricardocraft.client.runtime.utils.HWIDProvider;
import ru.ricardocraft.client.utils.helper.CommonHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;
import ru.ricardocraft.client.utils.helper.SecurityHelper;

import java.io.IOException;

public class RuntimeSecurityService {
    private final RequestService requestService;
    private final MessageManager messageManager;
    private final Boolean[] waitObject = new Boolean[]{null};

    public RuntimeSecurityService(RequestService requestService, MessageManager messageManager) {
        this.requestService = requestService;
        this.messageManager = messageManager;
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
                                new VerifySecureLevelKeyRequest(JavaFXApplication.publicKey.getEncoded(), signature))
                        .thenAccept((event1) -> {
                            if (!event1.needHardwareInfo) {
                                simpleGetHardwareToken();
                            } else {
                                doCollectHardwareInfo(!event1.onlyStatisticInfo);
                            }
                        }).exceptionally((e) -> {
                            messageManager.createNotification("Hardware Checker", e.getCause().getMessage());
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
                messageManager.createNotification("Hardware Checker", e.getCause().getMessage());
                notifyWaitObject(false);
                return null;
            });
        } catch (IOException e) {
            messageManager.createNotification("Hardware Checker", e.getCause().getMessage());
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
                    messageManager.createNotification("Hardware Checker", exc.getCause().getMessage());
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

    public boolean getSecurityState() throws InterruptedException {
        synchronized (waitObject) {
            if (waitObject[0] == null) waitObject.wait(3000);
            return waitObject[0];
        }
    }

    public byte[] sign(byte[] data) {
        return SecurityHelper.sign(data, JavaFXApplication.privateKey);
    }
}
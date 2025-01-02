package ru.ricardocraft.client.service.launch;

import ru.ricardocraft.client.service.LauncherTrustManager;

import java.security.cert.X509Certificate;

public class DebugLauncherTrustManager extends LauncherTrustManager {
    private final TrustDebugMode mode;

    public DebugLauncherTrustManager(TrustDebugMode mode) {
        super(new X509Certificate[0]);
        this.mode = mode;
    }

    @Override
    public CheckClassResult checkCertificates(X509Certificate[] certs, CertificateChecker checker) {
        if (mode == TrustDebugMode.TRUST_ALL) return new CheckClassResult(CheckClassResultType.SUCCESS, null, null);
        return super.checkCertificates(certs, checker);
    }

    public enum TrustDebugMode {
        TRUST_ALL
    }
}

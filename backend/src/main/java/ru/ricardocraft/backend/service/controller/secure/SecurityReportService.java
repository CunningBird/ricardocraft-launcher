package ru.ricardocraft.backend.service.controller.secure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.secure.SecurityReportRequest;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;

@Component
@RequiredArgsConstructor
public class SecurityReportService {

    private final ProtectHandler protectHandler;

    public SecurityReportResponse securityReport(SecurityReportRequest request, Client client) throws Exception {
        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            throw new Exception("Method not allowed");
        } else {
            return secureProtectHandler.onSecurityReport(request, client);
        }
    }
}

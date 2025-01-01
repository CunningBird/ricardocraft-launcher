package ru.ricardocraft.client.client;

import ru.ricardocraft.client.dto.request.auth.*;
import ru.ricardocraft.client.dto.response.*;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.dto.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.client.dto.request.management.FeaturesRequest;
import ru.ricardocraft.client.dto.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.client.dto.request.secure.SecurityReportRequest;
import ru.ricardocraft.client.dto.request.update.LauncherRequest;
import ru.ricardocraft.client.websockets.OfflineRequestService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClientLauncherMethods {

    public static void applyBasicOfflineProcessors(OfflineRequestService service) {
        service.registerRequestProcessor(LauncherRequest.class, (r) -> new LauncherRequestEvent(false, (String) null));
        service.registerRequestProcessor(CheckServerRequest.class, (r) -> {
            throw new RequestException("CheckServer disabled in offline mode");
        });
        service.registerRequestProcessor(GetAvailabilityAuthRequest.class, (r) -> {
            List<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> details = new ArrayList<>();
            details.add(new AuthLoginOnlyDetails());
            GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability = new GetAvailabilityAuthRequestEvent.AuthAvailability(details, "offline", "Offline Mode", true, new HashSet<>());
            List<GetAvailabilityAuthRequestEvent.AuthAvailability> list = new ArrayList<>(1);
            list.add(authAvailability);
            return new GetAvailabilityAuthRequestEvent(list);
        });
        service.registerRequestProcessor(JoinServerRequest.class, (r) -> new JoinServerRequestEvent(false));
        service.registerRequestProcessor(ExitRequest.class, (r) -> new ExitRequestEvent(ExitRequestEvent.ExitReason.CLIENT));
        service.registerRequestProcessor(SetProfileRequest.class, (r) -> new SetProfileRequestEvent(null));
        service.registerRequestProcessor(FeaturesRequest.class, (r) -> new FeaturesRequestEvent());
        service.registerRequestProcessor(GetSecureLevelInfoRequest.class, (r) -> new GetSecureLevelInfoRequestEvent(null, false));
        service.registerRequestProcessor(SecurityReportRequest.class, (r) -> new SecurityReportRequestEvent(SecurityReportRequestEvent.ReportAction.NONE));
    }
}

package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.response.TypeSerializeInterface;

import java.util.List;
import java.util.Set;

public class GetAvailabilityAuthResponse extends AbstractResponse {
    public final List<AuthAvailability> list;
    public final long features;

    public GetAvailabilityAuthResponse(List<AuthAvailability> list) {
        this.list = list;
        this.features = ServerFeature.FEATURE_SUPPORT.val;
    }

    public GetAvailabilityAuthResponse(List<AuthAvailability> list, long features) {
        this.list = list;
        this.features = features;
    }

    @Override
    public String getType() {
        return "getAvailabilityAuth";
    }

    public enum ServerFeature {
        FEATURE_SUPPORT(1);
        public final int val;

        ServerFeature(int val) {
            this.val = val;
        }
    }

    public interface AuthAvailabilityDetails extends TypeSerializeInterface {
    }

    public static class AuthAvailability {
        public final List<AuthAvailabilityDetails> details;
        public String name;
        public String displayName;

        public boolean visible;
        public Set<String> features;

        public AuthAvailability(List<AuthAvailabilityDetails> details, String name, String displayName, boolean visible, Set<String> features) {
            this.details = details;
            this.name = name;
            this.displayName = displayName;
            this.visible = visible;
            this.features = features;
        }
    }
}

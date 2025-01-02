package ru.ricardocraft.client.dto.response;

import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.base.utils.TypeSerializeInterface;

import java.util.List;
import java.util.Set;

public class GetAvailabilityAuthRequestEvent extends RequestEvent {

    public final List<AuthAvailability> list;
    public final long features;

    public GetAvailabilityAuthRequestEvent(List<AuthAvailability> list) {
        this.list = list;
        this.features = ServerFeature.FEATURE_SUPPORT.val;
    }

    public GetAvailabilityAuthRequestEvent(List<AuthAvailability> list, long features) {
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

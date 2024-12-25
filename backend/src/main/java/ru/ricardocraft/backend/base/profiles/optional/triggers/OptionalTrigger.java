package ru.ricardocraft.backend.base.profiles.optional.triggers;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.ricardocraft.backend.base.profiles.optional.OptionalFile;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OSTrigger.class, name = "os"),
})
public abstract class OptionalTrigger {
    public boolean required;
    public boolean inverted;

    protected abstract boolean isTriggered(OptionalFile optional, OptionalTriggerContext context);

    public boolean check(OptionalFile optional, OptionalTriggerContext context) {
        boolean result = isTriggered(optional, context);
        if (inverted) result = !result;
        return result;
    }
}

package ru.ricardocraft.backend.base.profiles.optional.triggers;

import ru.ricardocraft.backend.base.profiles.optional.OptionalFile;

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

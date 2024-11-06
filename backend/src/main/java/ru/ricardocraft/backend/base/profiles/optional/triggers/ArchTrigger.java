package ru.ricardocraft.backend.base.profiles.optional.triggers;

import ru.ricardocraft.backend.base.profiles.optional.OptionalFile;
import ru.ricardocraft.backend.helper.JVMHelper;

public class ArchTrigger extends OptionalTrigger {
    public JVMHelper.ARCH arch;

    @Override
    protected boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return context.getJavaVersion().arch == arch;
    }
}

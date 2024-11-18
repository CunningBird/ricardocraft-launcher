package ru.ricardocraft.backend.base.profiles.optional.triggers;

import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.base.profiles.optional.OptionalFile;

public class ArchTrigger extends OptionalTrigger {
    public JVMHelper.ARCH arch;

    @Override
    protected boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return context.getJavaVersion().arch == arch;
    }
}

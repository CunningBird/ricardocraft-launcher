package ru.ricardocraft.client.base.profiles.optional.triggers;

import ru.ricardocraft.client.base.profiles.optional.OptionalFile;
import ru.ricardocraft.client.utils.helper.JVMHelper;

public class ArchTrigger extends OptionalTrigger {
    public JVMHelper.ARCH arch;

    @Override
    protected boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return context.getJavaVersion().arch == arch;
    }
}

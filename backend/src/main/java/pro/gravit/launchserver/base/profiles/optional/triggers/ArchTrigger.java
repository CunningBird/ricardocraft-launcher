package pro.gravit.launchserver.base.profiles.optional.triggers;

import pro.gravit.launchserver.base.profiles.optional.OptionalFile;
import pro.gravit.launchserver.helper.JVMHelper;

public class ArchTrigger extends OptionalTrigger {
    public JVMHelper.ARCH arch;

    @Override
    protected boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return context.getJavaVersion().arch == arch;
    }
}

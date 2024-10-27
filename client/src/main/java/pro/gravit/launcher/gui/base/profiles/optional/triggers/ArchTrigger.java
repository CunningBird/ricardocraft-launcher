package pro.gravit.launcher.gui.base.profiles.optional.triggers;

import pro.gravit.launcher.gui.base.profiles.optional.OptionalFile;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTrigger;
import pro.gravit.launcher.gui.base.profiles.optional.triggers.OptionalTriggerContext;
import pro.gravit.launcher.gui.utils.helper.JVMHelper;

public class ArchTrigger extends OptionalTrigger {
    public JVMHelper.ARCH arch;

    @Override
    protected boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return context.getJavaVersion().arch == arch;
    }
}

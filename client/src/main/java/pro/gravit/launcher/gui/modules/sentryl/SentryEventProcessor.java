//package pro.gravit.launcher.gui.modules.sentryl;
//
//import io.sentry.EventProcessor;
//import io.sentry.Hint;
//import io.sentry.SentryEvent;
//import pro.gravit.launcher.gui.base.request.RequestException;
//import pro.gravit.launcher.gui.modules.sentryl.utils.OshiUtils;
//import pro.gravit.launcher.gui.utils.helper.JVMHelper;
//
//import java.util.Arrays;
//
//public class SentryEventProcessor implements EventProcessor {
//    @Override
//    public SentryEvent process(SentryEvent event, Hint hint) {
//        if (event.getThrowable() != null && SentryModule.config.ignoreErrors != null && SentryModule.config.ignoreErrors.contains(event.getThrowable().getMessage())) {
//            return null;
//        }
//        if (event.getThrowable() instanceof RequestException) {
//            event.setFingerprints(Arrays.asList("RequestException", event.getThrowable().getMessage()));
//        }
//        if (SentryModule.config.collectMemoryInfo) {
//            event.getContexts().put("Memory info", OshiUtils.makeMemoryProperties());
//        }
//        long uptime = JVMHelper.RUNTIME_MXBEAN.getUptime();
//        event.getContexts().put("Uptime", String.format("%ds %dms", uptime / 1000, uptime % 1000));
//        return event;
//    }
//}

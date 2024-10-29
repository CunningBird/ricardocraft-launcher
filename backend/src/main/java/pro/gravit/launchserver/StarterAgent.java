package pro.gravit.launchserver;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;

public final class StarterAgent {

    public static Instrumentation inst = null;
    public static Path libraries = null;

    public static void premain(String agentArgument, Instrumentation inst) {
        throw new UnsupportedOperationException("Please remove -javaagent option from start.sh");
    }
}

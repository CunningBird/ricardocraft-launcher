package pro.gravit.launchserver.core.serialize.stream;

import pro.gravit.launchserver.core.serialize.HInput;
import pro.gravit.launchserver.core.serialize.HOutput;
import pro.gravit.launchserver.utils.helper.IOHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class StreamObject {
    /* public StreamObject(HInput input) */

    public final byte[] write() throws IOException {
        try (ByteArrayOutputStream array = IOHelper.newByteArrayOutput()) {
            try (HOutput output = new HOutput(array)) {
                write(output);
            }
            return array.toByteArray();
        }
    }

    public abstract void write(HOutput output) throws IOException;


    @FunctionalInterface
    public interface Adapter<O extends StreamObject> {

        O convert(HInput input);
    }
}

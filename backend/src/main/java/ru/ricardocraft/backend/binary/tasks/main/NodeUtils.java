package ru.ricardocraft.backend.binary.tasks.main;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public final class NodeUtils {

    public static final int MAX_SAFE_BYTE_COUNT = 65535 - Byte.MAX_VALUE;

    private NodeUtils() {
    }

    public static InsnList getSafeStringInsnList(String string) {
        InsnList insnList = new InsnList();
        if ((string.length() * 3) < MAX_SAFE_BYTE_COUNT) { // faster multiModCheck
            insnList.add(new LdcInsnNode(string));
            return insnList;
        }

        insnList.add(new TypeInsnNode(NEW, "java/lang/StringBuilder"));
        insnList.add(new InsnNode(DUP));
        insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false));

        String[] chunks = splitUtf8ToChunks(string, MAX_SAFE_BYTE_COUNT);
        for (String chunk : chunks) {
            insnList.add(new LdcInsnNode(chunk));
            insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
        }
        insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));

        return insnList;
    }

    public static String[] splitUtf8ToChunks(String text, int maxBytes) {
        List<String> parts = new ArrayList<>();

        char[] chars = text.toCharArray();

        int lastCharIndex = 0;
        int currentChunkSize = 0;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            int charSize = getUtf8CharSize(c);
            if (currentChunkSize + charSize < maxBytes) {
                currentChunkSize += charSize;
            } else {
                parts.add(text.substring(lastCharIndex, i));
                currentChunkSize = 0;
                lastCharIndex = i;
            }
        }

        if (currentChunkSize != 0) {
            parts.add(text.substring(lastCharIndex));
        }

        return parts.toArray(new String[0]);
    }

    public static int getUtf8CharSize(char c) {
        if (c >= 0x0001 && c <= 0x007F) {
            return 1;
        } else if (c <= 0x07FF) {
            return 2;
        }
        return 3;
    }

    public static InsnList push(final int value) {
        InsnList ret = new InsnList();
        if (value >= -1 && value <= 5)
            ret.add(new InsnNode(Opcodes.ICONST_0 + value));
        else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
            ret.add(new IntInsnNode(Opcodes.BIPUSH, value));
        else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            ret.add(new IntInsnNode(Opcodes.SIPUSH, value));
        else
            ret.add(new LdcInsnNode(value));
        return ret;
    }

    public static InsnList makeValueEnumGetter(@SuppressWarnings("rawtypes") Enum u) {
        InsnList ret = new InsnList();
        Type e = Type.getType(u.getClass());
        ret.add(new FieldInsnNode(Opcodes.GETSTATIC, e.getInternalName(), u.name(), e.getDescriptor()));
        return ret;
    }
}

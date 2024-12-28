package ru.ricardocraft.backend.base.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.base.patcher.ClassTransformerPatcher;

import java.util.Locale;

@Slf4j
public class FindPacketHackPatcher extends ClassTransformerPatcher {

    public static final String READSTACK = "readItemStack".toLowerCase(Locale.US);
    public static final String READSTACK_SRG = "func_150791_c";

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (name != null) {
                            if (name.toLowerCase(Locale.US)
                                    .contains(READSTACK)
                                    || name.toLowerCase(Locale.US).contains(READSTACK_SRG))
                                log.info("Class {} method {} call to readItemStack and it may contain packethack!", reader.getClassName(), methodName);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        };
    }

}

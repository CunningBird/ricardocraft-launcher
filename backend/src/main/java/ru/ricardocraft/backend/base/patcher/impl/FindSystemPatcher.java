package ru.ricardocraft.backend.base.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;
import ru.ricardocraft.backend.base.patcher.ClassTransformerPatcher;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FindSystemPatcher extends ClassTransformerPatcher {

    private final List<String> noTriggeredMethods = new ArrayList<>();
    private final List<String> noTriggeredMethodsCL = new ArrayList<>();

    public FindSystemPatcher() {
        noTriggeredMethods.add("currentTimeMillis");
        noTriggeredMethods.add("getProperty");
        noTriggeredMethods.add("arraycopy");
        noTriggeredMethods.add("identityHashCode");
        noTriggeredMethods.add("nanoTime");

        noTriggeredMethodsCL.add("getSystemResource");
        noTriggeredMethodsCL.add("getSystemResourceAsStream");
        noTriggeredMethodsCL.add("getSystemResources");
    }

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKESTATIC && (("java/lang/System".equals(owner) && !noTriggeredMethods.contains(name)) || ("java/lang/ClassLoader".equals(owner) && !noTriggeredMethodsCL.contains(name)))) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        if ("defineClass".equals(name)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }
                };
            }
        };
    }
}

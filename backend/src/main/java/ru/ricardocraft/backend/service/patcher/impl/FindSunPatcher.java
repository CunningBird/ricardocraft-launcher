package ru.ricardocraft.backend.service.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;
import ru.ricardocraft.backend.service.patcher.ClassTransformerPatcher;

@Slf4j
public class FindSunPatcher extends ClassTransformerPatcher {

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (value instanceof String string && isUnsafe(string)) {
                    log.info("Class {} field {}: {}", reader.getClassName(), name, value);
                }
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (owner != null && isUnsafe(owner)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String string && isUnsafe(string)) {
                            log.info("Class {} method {} LDC {}", reader.getClassName(), methodName, value);
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        };
    }

    public boolean isUnsafe(String name) {
        if ((name.startsWith("com/sun/") || name.startsWith("com.sun.")) && !(name.startsWith("com/sun/jna") || name.startsWith("com.sun.jna")))
            return true;
        if (name.startsWith("jdk/") || name.startsWith("jdk.")) return true;
        return name.startsWith("sun/") || name.startsWith("sun.");
    }
}

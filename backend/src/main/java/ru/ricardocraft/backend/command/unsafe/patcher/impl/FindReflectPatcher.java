package ru.ricardocraft.backend.command.unsafe.patcher.impl;

import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.command.unsafe.patcher.ClassTransformerPatcher;

public class FindReflectPatcher extends ClassTransformerPatcher {

    private final Logger logger = LoggerFactory.getLogger(FindReflectPatcher.class);

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                if (value instanceof String string && isReflection(string)) {
                    logger.info("Class {} field {}: {}", reader.getClassName(), name, value);
                }
                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (owner != null && isReflection(owner)) {
                            logger.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String string && isReflection(string)) {
                            logger.info("Class {} method {} LDC {}", reader.getClassName(), methodName, value);
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        };
    }

    public boolean isReflection(String name) {
        return name.startsWith("java/lang/reflect/") || name.startsWith("java.lang.reflect.");
    }
}

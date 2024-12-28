package ru.ricardocraft.backend.base.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.base.patcher.ClassTransformerPatcher;

@Slf4j
public class FindDefineClassPatcher extends ClassTransformerPatcher {

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL && "java/lang/ClassLoader".equals(owner) && "defineClass".equals(name)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        } else if (opcode == Opcodes.INVOKEVIRTUAL && "java/security/SecureClassLoader".equals(owner) && "defineClass".equals(name)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String string && string.contains("defineClass")) {
                            // may be it is reflected call!
                            log.info("Class {} method {} LDC {}", reader.getClassName(), methodName, value);
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        };
    }
}

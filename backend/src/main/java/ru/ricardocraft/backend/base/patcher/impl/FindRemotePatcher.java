package ru.ricardocraft.backend.base.patcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.base.patcher.ClassTransformerPatcher;

@Slf4j
public class FindRemotePatcher extends ClassTransformerPatcher {

    @Override
    public ClassVisitor getVisitor(ClassReader reader, ClassWriter cw) {
        return new ClassVisitor(Opcodes.ASM7) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM7) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL && "java/net/URL".equals(owner) && "openConnection".equals(name)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        } else if (opcode == Opcodes.INVOKESPECIAL && "java/net/Socket".equals(owner) && "<init>".equals(name)) {
                            log.info("Class {} method {} call {}.{}({})", reader.getClassName(), methodName, owner, name, descriptor);
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String string && isHttpString(string)) {
                            log.info("Class {} method {} LDC {}", reader.getClassName(), methodName, value);
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        };
    }

    public boolean isHttpString(String value) {
        if (value.toLowerCase().startsWith("http://")) return true;
        return value.toLowerCase().startsWith("https://");
    }
}

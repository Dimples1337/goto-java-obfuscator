package org.gotoobfuscator.obj;

import org.gotoobfuscator.exceptions.MissingClassException;
import org.gotoobfuscator.utils.GotoClassWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public final class ClassWrapper {
    private final String name;
    private final ClassNode classNode;
    private final byte[] originalBytes;

    public ClassWrapper(byte[] data) {
        this.originalBytes = data;
        this.classNode = new ClassNode();

        final ClassReader reader = new ClassReader(data);
        reader.accept(classNode,ClassReader.SKIP_FRAMES);

        this.name = this.classNode.name;
    }

    public ClassWrapper(ClassNode classNode) {
        this.classNode = classNode;
        this.name = this.classNode.name;
        this.originalBytes = new byte[0];
    }

    public ClassWrapper(ClassNode classNode, byte[] originalBytes) {
        this.classNode = classNode;
        this.name = this.classNode.name;
        this.originalBytes = originalBytes;
    }

    public byte[] toByteArray(boolean useComputeMaxs) {
        GotoClassWriter writer = new GotoClassWriter(useComputeMaxs ? ClassWriter.COMPUTE_MAXS : ClassWriter.COMPUTE_FRAMES);

        try {
            classNode.accept(writer);

            return writer.toByteArray();
        } catch (TypeNotPresentException | MissingClassException e) {
            if (e instanceof TypeNotPresentException) {
                System.err.println("写出" + name + "时找不到类: " + ((TypeNotPresentException) e).typeName() + " 尝试使用COMPUTE_MAXS");
            } else {
                System.err.println("写出" + name + "时找不到类: " + ((MissingClassException) e).getMissingClassName() + " 尝试使用COMPUTE_MAXS");
            }
        } catch (Throwable e) {
            e.addSuppressed(new Throwable("写出" + name + "错误 尝试使用COMPUTE_MAXS"));

            e.printStackTrace();
        }

        try {
            writer = new GotoClassWriter(ClassWriter.COMPUTE_MAXS);

            classNode.accept(writer);

            return writer.toByteArray();
        } catch (Throwable e) {
            e.addSuppressed(new Throwable("无法写出 将返回空字节: " + name));
            e.printStackTrace();

            return new byte[0];
        }
    }

    public String getName() {
        return name;
    }

    public ClassNode getClassNode() {
        return classNode;
    }

    public byte[] getOriginalBytes() {
        return originalBytes;
    }
}

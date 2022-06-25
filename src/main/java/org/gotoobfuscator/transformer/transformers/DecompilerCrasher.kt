package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.InstructionBuilder
import org.gotoobfuscator.utils.InstructionModifier
import org.gotoobfuscator.utils.RandomUtils
import org.objectweb.asm.Handle
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LabelNode

class DecompilerCrasher : Transformer("DecompilerCrasher") {
    private val repeatType = "[".repeat(255)

    override fun transform(node: ClassNode) {
        for (method in node.methods) {
            if (method.instructions.size() == 0) continue
            if (method.name == "<init>") continue

            if (method.instructions.first !is LabelNode) continue

            val l = LabelNode()

            method.instructions.insertBefore(method.instructions.first,InstructionBuilder().apply {
                label(l)
                insn(ACONST_NULL)
                jump(IFNULL,method.instructions.first as LabelNode)

                insn(ACONST_NULL)
                type(CHECKCAST,"${repeatType}L;")
                invokeDynamic(RandomUtils.randomIllegalJavaName(),"(${repeatType}L;)V", Handle(H_INVOKESTATIC,RandomUtils.randomIllegalJavaName(),RandomUtils.randomIllegalJavaName(),"()L;",false),"")
            }.list)
        }
    }
}
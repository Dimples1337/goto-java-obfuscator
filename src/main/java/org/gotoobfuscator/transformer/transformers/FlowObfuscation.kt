package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.InstructionBuilder
import org.gotoobfuscator.utils.InstructionModifier
import org.gotoobfuscator.utils.RandomUtils
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.JumpInsnNode
import java.util.concurrent.ThreadLocalRandom

class FlowObfuscation : Transformer("FlowObfuscation") {
    override fun transform(node : ClassNode) {
        for (method in node.methods) {
            if (method.instructions.size() == 0) continue

            val modifier = InstructionModifier()

            val valueIndex = ++method.maxLocals

            var setupLocals = false

            for (instruction in method.instructions) {
                if (instruction is JumpInsnNode) {
                    when (instruction.opcode) {
                        GOTO -> {
                            modifier.replace(instruction, InstructionBuilder().apply {
                                varInsn(ILOAD,valueIndex)
                                jump(IFLT,instruction.label)
                                number(ThreadLocalRandom.current().nextInt())
                                varInsn(ISTORE,valueIndex)
                                type(NEW,"java/lang/RuntimeException")
                                insn(DUP)
                                ldc(RandomUtils.randomString(ThreadLocalRandom.current().nextInt(5,11), RandomUtils.UNICODE))
                                methodInsn(INVOKESPECIAL,"java/lang/RuntimeException","<init>","(Ljava/lang/String;)V",false);
                                insn(ATHROW)
                            }.list)

                            setupLocals = true
                        }
                    }
                }
            }

            if (setupLocals) {
                modifier.prepend(method.instructions.first, InstructionBuilder().apply {
                    number(ThreadLocalRandom.current().nextInt(Int.MIN_VALUE, 0))
                    varInsn(ISTORE, valueIndex)
                }.list)
            }

            modifier.apply(method)
        }
    }
}
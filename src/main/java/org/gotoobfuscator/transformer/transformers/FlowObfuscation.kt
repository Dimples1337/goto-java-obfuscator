package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.dictionary.impl.UnicodeDictionary
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.ASMUtils
import org.gotoobfuscator.utils.InstructionBuilder
import org.gotoobfuscator.utils.InstructionModifier
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier
import java.util.concurrent.ThreadLocalRandom

class FlowObfuscation : Transformer("FlowObfuscation") {
    override fun transform(node : ClassNode) {
        if (Modifier.isInterface(node.access)) return

        val dictionary = UnicodeDictionary(10)

        for (field in node.fields) {
            dictionary.addUsed(field.name)
        }

        val fieldName = dictionary.get()
        var setupField = false

        for (method in node.methods) {
            if (method.instructions.size() == 0) continue

            val modifier = InstructionModifier()

            for (instruction in method.instructions) {
                when (instruction) {
                    is JumpInsnNode -> {
                        when (instruction.opcode) {
                            GOTO -> {
                                modifier.replace(instruction, InstructionBuilder().apply {
                                    fieldInsn(GETSTATIC, node.name, fieldName, "I")
                                    jump(IFLT, instruction.label)

                                    var pop = false

                                    when (ThreadLocalRandom.current().nextInt(0, 5)) {
                                        0 -> {
                                            number(ThreadLocalRandom.current().nextInt())

                                            pop = true
                                        }
                                        1 -> {
                                            ldc(ThreadLocalRandom.current().nextLong())
                                        }
                                        2 -> {
                                            insn(ACONST_NULL)

                                            pop = true
                                        }
                                        3 -> {
                                            ldc(ThreadLocalRandom.current().nextFloat())

                                            pop = true
                                        }
                                        4 -> {
                                            ldc(ThreadLocalRandom.current().nextDouble())
                                        }
                                    }

                                    if (pop) {
                                        insn(POP)
                                    } else {
                                        insn(POP2)
                                    }

                                    insn(ACONST_NULL)
                                    insn(ATHROW)
                                }.list)

                                setupField = true
                            }
                        }
                    }
                    is VarInsnNode -> {
                        when (instruction.opcode) {
                            in ILOAD..ALOAD -> {
                                val l = LabelNode()

                                when (instruction.opcode) {
                                    LLOAD, DLOAD -> {
                                        method.maxLocals += 2
                                    }
                                    else -> {
                                        method.maxLocals++
                                    }
                                }

                                val index = method.maxLocals
                                val indexAnd = (method.maxLocals + 2).also { method.maxLocals = it }

                                modifier.append(instruction,InstructionBuilder().apply {
                                    varInsn(instruction.opcode + 33,index)
                                    varInsn(instruction.opcode,index)
                                    fieldInsn(GETSTATIC, node.name, fieldName, "I")
                                    jump(IFLT, l)

                                    fieldInsn(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;")
                                    ldc(ThreadLocalRandom.current().nextLong())
                                    methodInsn(INVOKEVIRTUAL,"java/io/PrintStream","println","(J)V",false)

                                    insn(ACONST_NULL)
                                    insn(ATHROW)
                                    label(l)
                                }.list)

                                setupField = true
                            }
                            in ISTORE..ASTORE -> {
                                modifier.append(instruction, InstructionBuilder().apply {
                                    varInsn(instruction.opcode - 33,instruction.`var`)

                                    if (instruction.opcode == DSTORE || instruction.opcode == LSTORE) {
                                        insn(POP2)
                                    } else {
                                        insn(POP)
                                    }
                                }.list)
                            }
                        }
                    }
                }
            }

            modifier.apply(method)
        }

        if (setupField) {
            val field = FieldNode(ACC_PRIVATE or ACC_STATIC,fieldName,"I",null,ThreadLocalRandom.current().nextInt(Int.MIN_VALUE,0))

            node.fields.add(field)
        }
    }
}
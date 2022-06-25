package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.dictionary.impl.UnicodeDictionary
import org.gotoobfuscator.obj.Resource
import org.gotoobfuscator.runtime.InvokeDynamicBootstrapMethod
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.InstructionModifier
import org.gotoobfuscator.utils.MethodDuplicator
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier

// 未完成
class InvokeDynamicProxy : Transformer("InvokeDynamicProxy") {
    private val desc = InvokeDynamicBootstrapMethod.DESC

    @Suppress("DuplicatedCode")
    override fun transform(node: ClassNode) {
        if (Modifier.isInterface(node.access)) return

        val dictionary = UnicodeDictionary(1)

        for (method in node.methods) {
            dictionary.addUsed(method.name)
        }

        val methodName = dictionary.get()
        var setup = false

        for (method in node.methods) {
            if (method.name == "<clinit>" || method.name == "<init>") continue

            val modifier = InstructionModifier()

            for (instruction in method.instructions) {
                when (instruction) {
                    is MethodInsnNode -> {
                        when (instruction.opcode) {
                            INVOKESTATIC -> {
                                modifier.replace(
                                    instruction,
                                    InvokeDynamicInsnNode(
                                        instruction.name,
                                        instruction.desc,
                                        Handle(H_INVOKESTATIC, node.name, methodName, desc, false),

                                        instruction.owner.replace("/","."),
                                        "",
                                        0
                                    )
                                )

                                setup = true
                            }
                            INVOKEVIRTUAL -> {
                                if (!instruction.owner.startsWith("[")) {
                                    modifier.replace(
                                        instruction,
                                        InvokeDynamicInsnNode(
                                            instruction.name,
                                            "(L${instruction.owner};${instruction.desc.substring(1)}",
                                            Handle(H_INVOKESTATIC, node.name, methodName, desc, false),

                                            instruction.owner.replace("/", "."),
                                            "",
                                            1
                                        )
                                    )

                                    setup = true
                                }
                            }
                        }
                    }
                    is FieldInsnNode -> {
                        when (instruction.opcode) {
                            GETSTATIC -> {
                                modifier.replace(
                                    instruction,
                                    InvokeDynamicInsnNode(
                                        instruction.name,
                                        "()${instruction.desc}",
                                        Handle(H_INVOKESTATIC, node.name, methodName, desc, false),

                                        instruction.owner.replace("/", "."),
                                        formatFieldDesc(instruction.desc),
                                        6
                                    )
                                    )

                                    setup = true
                            }
                            PUTSTATIC -> {
                                if (!instruction.desc.startsWith("[")) {
                                    modifier.replace(
                                        instruction,
                                        InvokeDynamicInsnNode(
                                            instruction.name,
                                            "(${instruction.desc})V",
                                            Handle(H_INVOKESTATIC, node.name, methodName, desc, false),

                                            instruction.owner.replace("/", "."),
                                            formatFieldDesc(instruction.desc),
                                            7
                                        )
                                    )

                                    setup = true
                                }
                            }
                        }
                    }
                }
            }

            modifier.apply(method)
        }

        if (setup) {
            node.methods.add(MethodDuplicator.copyMethod(InvokeDynamicBootstrapMethod::class.java,"bootstrap",desc,methodName))
        }
    }

    private fun formatFieldDesc(input : String) : String {
        return when (input) {
            "B","S","I","J","C","F","D","Z" -> {
                input
            }
            else -> {
                input.substring(1,input.length - 1).replace("/",".")
            }
        }
    }
}
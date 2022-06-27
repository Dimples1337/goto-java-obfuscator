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
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.lang.reflect.Modifier
import java.util.*

// 未完成
class InvokeDynamicProxy : Transformer("InvokeDynamicProxy") {
    private val bootstrapClassName : String
    private val staticMethodName : String
    private val virtualMethodName : String
    private val staticHandle : Handle
    private val virtualHandle : Handle

    init {
        val dictionary = UnicodeDictionary(1)

        bootstrapClassName = dictionary.get()
        staticMethodName = dictionary.get()
        virtualMethodName = dictionary.get()

        staticHandle = Handle(H_INVOKESTATIC, bootstrapClassName, staticMethodName, InvokeDynamicBootstrapMethod.STATIC_DESC, false)
        virtualHandle = Handle(H_INVOKESTATIC, bootstrapClassName, virtualMethodName, InvokeDynamicBootstrapMethod.VIRTUAL_DESC, false)
    }

    @Suppress("DuplicatedCode")
    override fun transform(node: ClassNode) {
        if (Modifier.isInterface(node.access)) return

        for (method in node.methods) {
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
                                        staticHandle,
                                        Type.getType(formatMethodDesc(instruction.owner))
                                    )
                                )
                            }
                        }
                    }
                }
            }

            modifier.apply(method)
        }
    }

    override fun finish(obfuscator: Obfuscator) {
        val node = ClassNode()

        node.visit(V1_8,ACC_PUBLIC,bootstrapClassName,null,"java/lang/Object",emptyArray())

        node.methods.add(MethodDuplicator.copyMethod(InvokeDynamicBootstrapMethod::class.java,"modeStatic",InvokeDynamicBootstrapMethod.STATIC_DESC,staticMethodName))

        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        node.accept(classWriter)

        obfuscator.resources.add(Resource("$bootstrapClassName.class",classWriter.toByteArray()))
    }

    private fun formatMethodDesc(input : String) : String {
        return if (input[0] == '[') {
            input
        } else {
            "L$input;"
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
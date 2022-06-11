package org.gotoobfuscator.packer

import org.apache.commons.io.IOUtils
import org.gotoobfuscator.transformer.SpecialTransformer
import org.gotoobfuscator.utils.ASMUtils
import org.gotoobfuscator.utils.InstructionModifier
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.experimental.inv

class ConstantPacker : SpecialTransformer("ConstantPacker") {
    companion object {
        private const val STRING = 0
        private const val DOUBLE = 1
        private const val FLOAT = 2
        private const val LONG = 3
        private const val INT = 4
    }

    private val list = LinkedList<Any>()

    private var index = 0

    @Suppress("DuplicatedCode")
    fun accept(node : ClassNode) {
        node.methods.forEach { method ->
            val modifier = InstructionModifier()

            method.instructions.forEach { insn ->
                when {
                    ASMUtils.isString(insn) -> {
                        val string = ASMUtils.getString(insn)

                        modifier.replace(insn,
                            FieldInsnNode(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;"),
                            ASMUtils.createNumberNode(index),
                            InsnNode(AALOAD),
                            TypeInsnNode(CHECKCAST,"java/lang/String")
                        )

                        push(string)
                    }
                    insn is LdcInsnNode -> {
                        when (insn.cst) {
                            is Int -> {
                                modifier.replace(insn,
                                    FieldInsnNode(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;"),
                                    ASMUtils.createNumberNode(index),
                                    InsnNode(AALOAD),
                                    TypeInsnNode(CHECKCAST,"java/lang/Integer"),
                                    MethodInsnNode(INVOKEVIRTUAL,"java/lang/Integer","intValue","()I")
                                )

                                push(insn.cst)
                            }
                            is Long -> {
                                modifier.replace(insn,
                                    FieldInsnNode(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;"),
                                    ASMUtils.createNumberNode(index),
                                    InsnNode(AALOAD),
                                    TypeInsnNode(CHECKCAST,"java/lang/Long"),
                                    MethodInsnNode(INVOKEVIRTUAL,"java/lang/Long","longValue","()J")
                                )

                                push(insn.cst)
                            }
                            is Double -> {
                                modifier.replace(insn,
                                    FieldInsnNode(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;"),
                                    ASMUtils.createNumberNode(index),
                                    InsnNode(AALOAD),
                                    TypeInsnNode(CHECKCAST,"java/lang/Double"),
                                    MethodInsnNode(INVOKEVIRTUAL,"java/lang/Double","doubleValue","()D")
                                )

                                push(insn.cst)
                            }
                            is Float -> {
                                modifier.replace(insn,
                                    FieldInsnNode(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;"),
                                    ASMUtils.createNumberNode(index),
                                    InsnNode(AALOAD),
                                    TypeInsnNode(CHECKCAST,"java/lang/Float"),
                                    MethodInsnNode(INVOKEVIRTUAL,"java/lang/Float","floatValue","()F")
                                )

                                push(insn.cst)
                            }
                        }
                    }
                }
            }

            modifier.apply(method)
        }
    }

    private fun push(o : Any) {
        list.add(o)

        index++
    }

    fun buildClass() : ByteArray {
        return IOUtils.toByteArray(ConstantPacker::class.java.getResourceAsStream("/org/gotoobfuscator/runtime/Const.class"))
    }

    fun build() : ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        dos.writeInt(list.size)

        list.forEach { o ->
            when (o) {
                is String -> {
                    dos.write(STRING)
                    dos.writeUTF(o)
                }
                is Double -> {
                    dos.write(DOUBLE)
                    dos.writeDouble(o)
                }
                is Float -> {
                    dos.write(FLOAT)
                    dos.writeFloat(o)
                }
                is Long -> {
                    dos.write(LONG)
                    dos.writeLong(o)
                }
                is Int -> {
                    dos.write(INT)
                    dos.writeInt(o)
                }
            }
        }

        dos.close()

        return bos.toByteArray()
    }
}
package org.gotoobfuscator.packer

import org.apache.commons.io.IOUtils
import org.gotoobfuscator.transformer.SpecialTransformer
import org.gotoobfuscator.utils.ASMUtils
import org.gotoobfuscator.utils.InstructionBuilder
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
                            getFromArray("java/lang/String", emptyArray())
                        )

                        push(string)
                    }
                    insn is LdcInsnNode -> {
                        when (insn.cst) {
                            is Int -> {
                                modifier.replace(insn,
                                    getFromArray("java/lang/Integer", arrayOf(MethodInsnNode(INVOKEVIRTUAL,"java/lang/Integer","intValue","()I")))
                                )

                                push(insn.cst)
                            }
                            is Long -> {
                                modifier.replace(insn,
                                    getFromArray("java/lang/Long", arrayOf(MethodInsnNode(INVOKEVIRTUAL,"java/lang/Long","longValue","()J")))
                                )

                                push(insn.cst)
                            }
                            is Double -> {
                                modifier.replace(insn,
                                    getFromArray("java/lang/Double", arrayOf(MethodInsnNode(INVOKEVIRTUAL,"java/lang/Double","doubleValue","()D")))
                                )

                                push(insn.cst)
                            }
                            is Float -> {
                                modifier.replace(insn,
                                    getFromArray("java/lang/Float", arrayOf(MethodInsnNode(INVOKEVIRTUAL,"java/lang/Float","floatValue","()F")))
                                )

                                push(insn.cst)
                            }
                        }
                    }
                    insn is IntInsnNode -> {
                        modifier.replace(insn,
                            getFromArray("java/lang/Integer", arrayOf(MethodInsnNode(INVOKEVIRTUAL,"java/lang/Integer","intValue","()I")))
                        )

                        push(insn.operand)
                    }
                }
            }

            modifier.apply(method)
        }
    }

    private fun getFromArray(castTo : String, others : Array<AbstractInsnNode>) : InsnList {
        return InstructionBuilder().apply {
            fieldInsn(GETSTATIC,"org/gotoobfuscator/runtime/Const","ARRAY","[Ljava/lang/Object;")
            number(index)
            insn(AALOAD)
            type(CHECKCAST,castTo)
            addArray(others)
        }.list
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
                is Short -> {
                    dos.write(INT)
                    dos.writeInt(o.toInt())
                }
                is Byte -> {
                    dos.write(INT)
                    dos.writeInt(o.toInt())
                }
            }
        }

        dos.close()

        return bos.toByteArray()
    }
}
package org.gotoobfuscator.utils

import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicVerifier
import java.lang.RuntimeException

object MethodDuplicator {
    fun copyMethod(c : Class<*>,methodName : String,methodDesc : String,copyMethodName : String) : MethodNode {
        MethodDuplicator::class.java.getResourceAsStream("/${c.name.replace(".","/")}.class").use {
            val reader = ClassReader(IOUtils.toByteArray(it))
            val node = ClassNode()

            reader.accept(node,0)

            for (method in node.methods) {
                if (method.name == methodName && method.desc == methodDesc) {
                    val methodNode = MethodNode(method.access,copyMethodName,method.desc,method.signature,method.exceptions.toTypedArray())

                    methodNode.instructions.add(method.instructions.apply {
                        for (abstractInsnNode in toArray()) {
                            when (abstractInsnNode) {
                                is LineNumberNode -> {
                                    remove(abstractInsnNode)
                                }
                            }
                        }
                    })

                    methodNode.tryCatchBlocks.addAll(method.tryCatchBlocks)
                    methodNode.maxStack = method.maxStack
                    methodNode.maxLocals = method.maxLocals

                    return methodNode
                }
            }
        }

        throw RuntimeException("Not found: ${c.name.replace(".","/")}.$methodName$methodDesc")
    }
}
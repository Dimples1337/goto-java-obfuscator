package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.dictionary.impl.UnicodeDictionary
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.InstructionModifier
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

class InvokeProxy : Transformer("InvokeProxy") {
    private var count = 0

    override fun transform(node : ClassNode) {
        val syntheticMethods = ArrayList<MethodNode>()
        val dictionary = UnicodeDictionary(2)

        for (method in node.methods) {
            val modifier = InstructionModifier()

            for (instruction in method.instructions) {
                when (instruction) {
                    is MethodInsnNode -> {
                        when (instruction.opcode) {
                            INVOKESTATIC -> {
                                val methodName = dictionary.get()
                                val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC,methodName,instruction.desc,null,null)
                                val type = Type.getReturnType(instruction.desc)

                                visitArgs(0,Type.getArgumentTypes(instruction.desc),methodNode)
                                methodNode.visitMethodInsn(INVOKESTATIC,instruction.owner,instruction.name,instruction.desc,instruction.itf)
                                visitReturn(type, methodNode)

                                syntheticMethods.add(methodNode)
                                modifier.replace(
                                    instruction,
                                    MethodInsnNode(INVOKESTATIC,node.name,methodName,instruction.desc,false)
                                )

                                count++
                            }
                            INVOKEVIRTUAL -> {
                                val types = Type.getArgumentTypes(instruction.desc)
                                val desc = Array<Type>(types.size + 1) {
                                    if (it == 0) {
                                        Type.getObjectType(instruction.owner)
                                    } else {
                                        types[it - 1]
                                    }
                                }

                                val methodName = dictionary.get()
                                val type = Type.getReturnType(instruction.desc)
                                val methodDesc = Type.getMethodDescriptor(type, *desc)
                                val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC, methodName, methodDesc, null, null)

                                methodNode.visitVarInsn(ALOAD, 0)
                                visitArgs(1, types, methodNode)
                                methodNode.visitMethodInsn(INVOKEVIRTUAL, instruction.owner, instruction.name, instruction.desc, instruction.itf)
                                visitReturn(type, methodNode)

                                syntheticMethods.add(methodNode)
                                modifier.replace(
                                    instruction,
                                    MethodInsnNode(INVOKESTATIC, node.name, methodName, methodDesc, false)
                                )

                                count++
                            }
                        }
                    }
                    is FieldInsnNode -> {
                        when (instruction.opcode) {
                            GETSTATIC -> {
                                val type = Type.getType(instruction.desc)
                                val methodDescriptor = Type.getMethodDescriptor(type)
                                val methodName = dictionary.get()
                                val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC,methodName, methodDescriptor,null,null)

                                methodNode.visitFieldInsn(GETSTATIC,instruction.owner,instruction.name,instruction.desc)
                                visitReturn(type, methodNode)

                                syntheticMethods.add(methodNode)
                                modifier.replace(instruction,
                                    MethodInsnNode(INVOKESTATIC,node.name,methodName,methodDescriptor,false)
                                )

                                count++
                            }
                            PUTSTATIC -> {
                                val type = Type.getType(instruction.desc)
                                val methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE,type)
                                val methodName = dictionary.get()
                                val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC,methodName, methodDescriptor,null,null)

                                visitArgs(0, arrayOf(type), methodNode)
                                methodNode.visitFieldInsn(PUTSTATIC,instruction.owner,instruction.name,instruction.desc)
                                methodNode.visitInsn(RETURN)

                                syntheticMethods.add(methodNode)
                                modifier.replace(instruction,
                                    MethodInsnNode(INVOKESTATIC,node.name,methodName,methodDescriptor,false)
                                )

                                count++
                            }
                            GETFIELD -> {
                                if (!method.name.equals("<init>")) {
                                    val type = Type.getType(instruction.desc)
                                    val objectType = Type.getObjectType(instruction.owner)
                                    val methodDescriptor = Type.getMethodDescriptor(type, objectType)
                                    val methodName = dictionary.get()
                                    val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC, methodName, methodDescriptor, null, null)

                                    visitArgs(0, arrayOf(objectType), methodNode)
                                    methodNode.visitFieldInsn(GETFIELD, instruction.owner, instruction.name, instruction.desc)
                                    visitReturn(type, methodNode)

                                    syntheticMethods.add(methodNode)
                                    modifier.replace(
                                        instruction,
                                        MethodInsnNode(INVOKESTATIC, node.name, methodName, methodDescriptor, false)
                                    )

                                    count++
                                }
                            }
                            PUTFIELD -> {
                                if (!method.name.equals("<init>")) {
                                    val type = Type.getType(instruction.desc)
                                    val objectType = Type.getObjectType(instruction.owner)
                                    val methodDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, objectType, type)
                                    val methodName = dictionary.get()
                                    val methodNode = MethodNode(ACC_PRIVATE or ACC_STATIC, methodName, methodDescriptor, null, null)

                                    visitArgs(0, arrayOf(objectType, type), methodNode)
                                    methodNode.visitFieldInsn(PUTFIELD, instruction.owner, instruction.name, instruction.desc)
                                    methodNode.visitInsn(RETURN)

                                    syntheticMethods.add(methodNode)
                                    modifier.replace(
                                        instruction,
                                        MethodInsnNode(INVOKESTATIC, node.name, methodName, methodDescriptor, false)
                                    )

                                    count++
                                }
                            }
                        }
                    }
                }
            }

            modifier.apply(method)
        }

        node.methods.addAll(syntheticMethods)
    }

    override fun finish(obfuscator: Obfuscator) {
        print("Replaced $count invokes")
    }

    private fun visitArgs(offset : Int,types : Array<Type>,methodNode: MethodNode) {
        var index = offset

        for (type in types) {
            when (type) {
                Type.INT_TYPE, Type.SHORT_TYPE, Type.BYTE_TYPE, Type.CHAR_TYPE, Type.BOOLEAN_TYPE -> {
                    methodNode.visitVarInsn(ILOAD,index)
                }
                Type.LONG_TYPE -> {
                    methodNode.visitVarInsn(LLOAD,index)
                }
                Type.FLOAT_TYPE -> {
                    methodNode.visitVarInsn(FLOAD,index)
                }
                Type.DOUBLE_TYPE -> {
                    methodNode.visitVarInsn(DLOAD,index)
                }
                else -> {
                    methodNode.visitVarInsn(ALOAD,index)
                }
            }

            if (type == Type.DOUBLE_TYPE || type == Type.LONG_TYPE) {
                index += 2
            } else {
                index++
            }
        }
    }

    private fun visitReturn(type : Type,methodNode : MethodNode) {
        if (type.sort == Type.METHOD) {
            methodNode.visitInsn(RETURN)
        } else {
            when (type) {
                Type.VOID_TYPE -> {
                    methodNode.visitInsn(RETURN)
                }
                Type.INT_TYPE, Type.BOOLEAN_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE, Type.BYTE_TYPE -> {
                    methodNode.visitInsn(IRETURN)
                }
                Type.FLOAT_TYPE -> {
                    methodNode.visitInsn(FRETURN)
                }
                Type.DOUBLE_TYPE -> {
                    methodNode.visitInsn(DRETURN)
                }
                Type.LONG_TYPE -> {
                    methodNode.visitInsn(LRETURN)
                }
                else -> {
                    methodNode.visitInsn(ARETURN)
                }
            }
        }
    }
}
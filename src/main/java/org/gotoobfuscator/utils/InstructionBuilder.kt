package org.gotoobfuscator.utils

import org.objectweb.asm.Handle
import org.objectweb.asm.tree.*

@Suppress("SpellCheckingInspection")
class InstructionBuilder {
    val list = InsnList()

    fun insn(opcode : Int) {
        list.add(InsnNode(opcode))
    }

    fun intInsn(opcode : Int,operand : Int) {
        list.add(IntInsnNode(opcode, operand))
    }

    fun varInsn(opcode : Int, index : Int) {
        list.add(VarInsnNode(opcode, index))
    }

    fun type(opcode : Int, type : String) {
        list.add(TypeInsnNode(opcode, type))
    }

    fun fieldInsn(opcode : Int, owner : String, name : String, desc : String) {
        list.add(FieldInsnNode(opcode, owner, name, desc))
    }

    fun methodInsn(opcode : Int, owner : String, name : String, desc : String, isInterface : Boolean) {
        list.add(MethodInsnNode(opcode, owner, name, desc, isInterface))
    }

    fun invokeDynamic(name : String, desc : String, handle : Handle, vararg args : Any) {
        list.add(InvokeDynamicInsnNode(name, desc, handle, *args))
    }

    fun jump(opcode : Int, label : LabelNode) {
        list.add(JumpInsnNode(opcode, label))
    }

    fun ldc(value : Any) {
        list.add(LdcInsnNode(value))
    }

    fun iinc(index : Int, increment : Int) {
        list.add(IincInsnNode(index, increment))
    }

    fun tableSwitch(min : Int, max : Int, defaultLabel : LabelNode, vararg labels : LabelNode) {
        list.add(TableSwitchInsnNode(min, max, defaultLabel, *labels))
    }

    fun lookupSwitch(defaultLabel : LabelNode, keys : IntArray, labels : Array<LabelNode>) {
        list.add(LookupSwitchInsnNode(defaultLabel, keys, labels))
    }

    fun multiANewArray(desc : String, numDimensions : Int) {
        list.add(MultiANewArrayInsnNode(desc, numDimensions))
    }

    fun number(i : Int) {
        list.add(ASMUtils.createNumberNode(i))
    }

    fun addList(list : InsnList) {
        this.list.add(list)
    }

    fun add(node : AbstractInsnNode) {
        list.add(node)
    }

    fun label(l : LabelNode) {
        list.add(l)
    }

    fun addArray(array : Array<AbstractInsnNode>) {
        for (abstractInsnNode in array) {
            list.add(abstractInsnNode)
        }
    }
}
package org.gotoobfuscator.interfaces

import org.objectweb.asm.tree.ClassNode

interface ClassManager {
    fun isLibNode(node : ClassNode) : Boolean

    fun getClassNode(name : String) : ClassNode
}
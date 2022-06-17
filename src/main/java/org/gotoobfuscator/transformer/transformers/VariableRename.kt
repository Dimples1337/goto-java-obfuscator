package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.dictionary.ListDictionary
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.RandomUtils
import org.objectweb.asm.tree.ClassNode

class VariableRename : Transformer("VariableRename") {
    override fun transform(node: ClassNode) {
        val localVariablesDictionary = object : ListDictionary(10) {
            private val list = arrayListOf("I","i")

            override fun getList(): List<String> {
                return list
            }
        }

        for (method in node.methods) {
            localVariablesDictionary.reset()

            if (method.localVariables != null && method.localVariables.isNotEmpty()) {
                for (localVariable in method.localVariables) {
                    if (localVariable.name != "this")
                        localVariable.name = localVariablesDictionary.get()
                }
            }
        }
    }
}
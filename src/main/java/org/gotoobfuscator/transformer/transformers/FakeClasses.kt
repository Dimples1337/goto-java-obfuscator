package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.dictionary.impl.UnicodeDictionary
import org.gotoobfuscator.obj.Resource
import org.gotoobfuscator.transformer.Transformer
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import java.util.concurrent.ThreadLocalRandom

class FakeClasses : Transformer("FakeClasses") {
    override fun onStart(obfuscator: Obfuscator) {
        val d = UnicodeDictionary(5)

        for (c in obfuscator.allClasses) {
            d.addUsed(c.value.classNode.name)
        }

        repeat(ThreadLocalRandom.current().nextInt(50,101)) {
            val name = d.get()

            val classWriter = ClassWriter(0)

            val classNode = ClassNode()

            classNode.visit(V1_8, ACC_PUBLIC, name, null, name, arrayOf(name,name))

            val method = MethodNode(ACC_PUBLIC or ACC_STATIC, "", "", null, null)

            method.instructions.add(LdcInsnNode(""))

            classNode.methods.add(method)

            classNode.accept(classWriter)

            obfuscator.resources["$name.class"] = Resource("$name.class",classWriter.toByteArray())
        }
    }
}
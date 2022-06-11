package org.gotoobfuscator.transformer.transformers

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.obj.Resource
import org.gotoobfuscator.transformer.Transformer
import org.gotoobfuscator.utils.RandomUtils
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.concurrent.ThreadLocalRandom

class Crasher : Transformer("Crasher") {
    override fun onStart(obfuscator: Obfuscator) {
        for (i in 0..100) {
            setup(null,obfuscator)
            setup("META-INF/",obfuscator);
        }
    }

    private fun setup(basePath : String?,obfuscator : Obfuscator) {
        val node = ClassNode()

        node.name = "<html><img src=\"https:" + RandomUtils.randomString(10,"0123456789")
        node.access = ACC_PUBLIC
        node.version = V1_8

        val writer = ClassWriter(0)
        node.accept(writer)

        val builder = StringBuilder()

        if (basePath != null) {
            builder.append(basePath)
        }

        for (i in 0..ThreadLocalRandom.current().nextInt(250,500)) {
            builder.append("\n\u3000\u2007".random()).append(File.separator)
        }

        builder.append(randomSpace()).append(".class")

        val s = builder.toString()

        obfuscator.resources[s] = Resource(s,writer.toByteArray())
    }

    private fun randomSpace() : String {
        return RandomUtils.randomString(ThreadLocalRandom.current().nextInt(5,10),"\n\u3000\u2007")
    }
}
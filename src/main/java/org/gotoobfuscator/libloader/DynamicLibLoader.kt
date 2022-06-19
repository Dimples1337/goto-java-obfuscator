package org.gotoobfuscator.libloader

import org.apache.commons.io.IOUtils
import org.gotoobfuscator.exceptions.MissingClassException
import org.gotoobfuscator.interfaces.ClassManager
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.Closeable
import java.io.File
import java.util.jar.JarFile

class DynamicLibLoader(files : ArrayList<File>) : Closeable,ClassManager {
    private val jars = ArrayList<JarFile>()
    private val classes = HashMap<String,ClassNode>()

    init {
        for (file in files) {
            if (file.isDirectory) {
                file.listFiles()?.forEach {
                    jars.add(JarFile(it))
                }
            } else {
                jars.add(JarFile(file))
            }
        }
    }

    override fun isLibNode(node: ClassNode): Boolean {
        return try {
            getClassNode(node.name)

            true
        } catch (e : MissingClassException) {
            false
        }
    }

    override fun getClassNode(name : String) : ClassNode {
        val inMap = classes[name]

        if (inMap != null) return inMap

        for (jar in jars) {
            val e = jar.getJarEntry("${name}.class")

            if (e != null) {
                jar.getInputStream(e).use { stream ->
                    val data = IOUtils.toByteArray(stream)

                    val classReader = ClassReader(data)
                    val classNode = ClassNode()

                    classReader.accept(classNode,0)

                    return classNode.also { classes[name] = it }
                }
            }
        }

        throw MissingClassException(name)
    }

    override fun close() {
        for (jar in jars) {
            jar.close()
        }
    }
}
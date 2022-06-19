package org.gotoobfuscator.libloader

import org.apache.commons.io.IOUtils
import org.gotoobfuscator.exceptions.MissingClassException
import org.gotoobfuscator.interfaces.ClassManager
import org.gotoobfuscator.obj.ClassWrapper
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.jar.JarFile

class StaticLibLoader(private val libraries : ArrayList<File>,
                      private val multiThreadLoadLibraries : Boolean) : ClassManager {
    private val classes = ConcurrentHashMap<String, ClassWrapper>()
    private val isLibMap = HashMap<ClassNode,Boolean>()

    override fun isLibNode(node: ClassNode): Boolean {
        val b = isLibMap[node]

        if (b != null) return b

        for (libClass in classes.values) {
            if (libClass.classNode == node) {
                isLibMap[node] = true
                return true
            }
        }

        isLibMap[node] = false
        return false
    }

    private val nodeMap = HashMap<String,ClassNode>()

    override fun getClassNode(name: String): ClassNode {
        val fromMap = nodeMap[name]

        if (fromMap != null) return fromMap

        val classWrapper = classes.values.find { it.classNode.name == name } ?: throw MissingClassException(name)

        return classWrapper.classNode.also {
            nodeMap[name] = it
        }
    }

    fun loadLibraries(threadPool : ThreadPoolExecutor) {
        println("Loading libraries MultiThreadLoadLibraries:${multiThreadLoadLibraries}")

        libraries.forEach { libFile ->
            if (libFile.isDirectory) {
                libFile.listFiles()?.forEach {
                    try {
                        if (multiThreadLoadLibraries) {
                            threadPool.execute(SingleLibraryLoader(it))
                        } else {
                            SingleLibraryLoader(it).run()
                        }
                    } catch (e : Throwable) {
                        e.printStackTrace()
                    }
                }
            } else {
                try {
                    if (multiThreadLoadLibraries) {
                        threadPool.execute(SingleLibraryLoader(libFile))
                    } else {
                        SingleLibraryLoader(libFile).run()
                    }
                } catch (e : Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class SingleLibraryLoader(private val libFile : File) : Runnable {
        override fun run() {
            var loadedClasses = 0

            JarFile(libFile).use { file ->
                for (entry in file.entries()) {
                    val name = entry.name

                    if (name.endsWith(".class")) {
                        file.getInputStream(entry).use { stream ->
                            val data = IOUtils.toByteArray(stream)

                            val classReader = ClassReader(data)
                            val classNode = ClassNode()

                            classReader.accept(classNode,0)

                            classes[name] = ClassWrapper(classNode,data)

                            loadedClasses++
                        }
                    }
                }
            }

            println("Loaded $loadedClasses classes from ${libFile.absolutePath}")
        }
    }
}
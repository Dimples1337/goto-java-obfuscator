package org.gotoobfuscator

import com.google.gson.GsonBuilder
import org.gotoobfuscator.plugin.PluginManager
import org.gotoobfuscator.transformer.transformers.*
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

object Main {
    private const val version = "6.4"

    private val gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun main(args : Array<String>) {
        println("--- Goto obfuscator $version ---")
        println("By 亚蓝")
        println("QQ交流群 & 更新群: 340714894")

        kfc()

        if (args.size < 2) {
            printHelp()
            return
        }

        val file = File(args[1])

        when (args[0].lowercase()) {
            "process" -> {
                process(file)
            }
            "build" -> {
                build(file)
            }
            else -> {
                printHelp()
            }
        }
    }

    private fun kfc() {
        val calendar = Calendar.getInstance()

        calendar.time = Date()

        if (calendar.get(Calendar.DAY_OF_WEEK) == 5) {
            println("今天是疯狂星期四!")
        }
    }

    private fun printHelp() {
        println("process <Config file name> -> 使用现有的配置进行")
        println("build <Config file name> -> 构建一个配置")
    }

    private fun process(configFile : File) {
        if (!configFile.exists()) {
            println("${configFile.absolutePath} 不存在!")
            return
        }

        val config : Config

        configFile.bufferedReader(StandardCharsets.UTF_8).use {
            config = gson.fromJson(it.readText(), Config::class.java)
        }

        val obfuscator = Obfuscator(File(config.input), File(config.output))

        if (config.mainClass.isNotEmpty()) {
            obfuscator.mainClass = config.mainClass
        }

        for (path in config.libraries) {
            val file = File(path)

            if (!file.exists()) {
                System.err.println("WARNING: Library file: ${file.absolutePath} not exists!")
                continue
            }

            println("Found library ${if (file.isDirectory) "directory" else "file"}: ${file.absolutePath}")
            obfuscator.addLibraries(file)
        }

        obfuscator.addExtractZips(config.extractZips)

        obfuscator.addSkipClasses(config.skipClasses)
        obfuscator.addExcludeClassNames(config.excludeClasses)

        obfuscator.dictionaryFile = config.classRenameDictionaryFile
        obfuscator.classRenamePackageName = config.classRenamePackageName

        ClassRename.exclude.addAll(config.classRenameExclude)

        obfuscator.corruptCRC = config.corruptCRC
        obfuscator.corruptDate = config.corruptDate
        obfuscator.classFolder = config.classFolder
        obfuscator.duplicateResource = config.duplicateResource
        obfuscator.extractorMode = config.extractorMode
        obfuscator.dictionaryMode = config.classRenameDictionaryMode
        obfuscator.useComputeMaxs = config.useComputeMaxs
        obfuscator.multiThreadLoadLibraries = config.multiThreadLoadLibraries
        obfuscator.preVerify = config.preVerify
        obfuscator.classRenameRemoveMetadata = config.classRenameRemoveMetadata
        obfuscator.libMode = config.libMode

        obfuscator.setPackerEnable(config.packerEnable)
        obfuscator.setConstantPackerEnable(config.constantPackerEnable)

        obfuscator.threadPoolSize = config.threadPoolSize
        obfuscator.dictionaryRepeatTimeBase  = config.dictionaryRepeatTimeBase

        if (config.classRenameEnable) obfuscator.addTransformers(ClassRename())
        if (config.stringEncryptorEnable) obfuscator.addTransformers(StringEncryptor())
        if (config.numberEncryptorEnable) obfuscator.addTransformers(NumberEncryptor())
        if (config.sourceRenameEnable) obfuscator.addTransformers(SourceRename())
        if (config.crasherEnable) obfuscator.addTransformers(Crasher())
        if (config.invalidSignatureEnable) obfuscator.addTransformers(InvalidSignature())
        if (config.invokeProxyEnable) obfuscator.addTransformers(InvokeProxy())
        if (config.variableRenameEnable) obfuscator.addTransformers(VariableRename())
        if (config.decompilerCrasherEnable) obfuscator.addTransformers(DecompilerCrasher())
        if (config.junkCodeEnable) obfuscator.addTransformers(JunkCode())
        if (config.flowObfuscationEnable) obfuscator.addTransformers(FlowObfuscation())
        if (config.badAnnotationEnable) obfuscator.addTransformers(BadAnnotation())
        if (config.hideCodeEnable) obfuscator.addTransformers(HideCode())
        if (config.fakeClassesEnable) obfuscator.addTransformers(FakeClasses())

        PluginManager(obfuscator).searchPlugins()

        obfuscator.start()
    }

    private fun build(configFile : File) {
        println("尝试写出配置...")

        val config = Config()

        FileOutputStream(configFile).use {
            it.write(gson.toJson(config).toByteArray(StandardCharsets.UTF_8))
        }

        println("配置保存于: ${configFile.absolutePath}")
    }
}
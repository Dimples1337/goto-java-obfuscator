package org.gotoobfuscator.dictionary.impl

import org.gotoobfuscator.Obfuscator
import org.gotoobfuscator.dictionary.IDictionary
import org.gotoobfuscator.dictionary.ListDictionary
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class CustomDictionary : ListDictionary(Obfuscator.Instance.dictionaryRepeatTimeBase) {
    companion object {
        @JvmStatic
        val list = ArrayList<String>()

        init {
            FileInputStream(Obfuscator.Instance.dictionaryFile).bufferedReader(StandardCharsets.UTF_8).use { list.addAll(it.readLines()) }
        }
    }

    override fun getList(): List<String> {
        return list
    }
}
package org.gotoobfuscator.dictionary.impl

import org.gotoobfuscator.dictionary.ListDictionary

class UnicodeDictionary(repeatTime : Int) : ListDictionary(repeatTime) {
    companion object {
        val arabic = ArrayList<String>()
        val unicode = ArrayList<String>()

        init {
            //for (i in 13312..40956) { //中文
            //    chinese.add(i.toChar())
            //}

            for (i in 0x060C..0x06FE) { //阿拉伯文
                arabic.add(i.toChar().toString())
            }

            unicode.addAll(
                arrayOf(
                    "\u0489",
                    "\u034C",
                    "\u035C",
                    "\u034E",
                    "\u0344",
                    "\u0306",
                    "\u0307",
                    "\u0321",
                    "\u0331"
                )
            )
        }
    }

    override fun getList(): List<String> {
        return unicode
    }
}
package org.gotoobfuscator.dictionary

abstract class ListDictionary(private val baseRepeatTime : Int) : IDictionary {
    private val used = ArrayList<String>()

    override fun get() : String {
        val list = getList()
        var s : String
        var ticks = 0
        var repeatTime = baseRepeatTime

        do {
            s = run {
                val b = StringBuilder()

                repeat(repeatTime) {
                    b.append(list.random())
                }

                return@run b.toString()
            }

            if (ticks == list.size) {
                repeatTime++
                ticks = 0
            }

            ticks++
        } while (used.contains(s))

        used.add(s)

        return s
    }

    abstract fun getList() : List<String>
}
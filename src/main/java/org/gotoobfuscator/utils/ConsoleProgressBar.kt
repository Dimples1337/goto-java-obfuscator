package org.gotoobfuscator.utils

import java.text.DecimalFormat

class ConsoleProgressBar(private val max : Double) {
    private val formatter = DecimalFormat("#.##%")

    private var rate = 0.0

    private fun draw(info : String) {
        print('\r')

        print('[')

        val length = (10.0 * rate).toInt()

        for (i in 0..length)
            print('=')

        for (i in 0 until (10 - length))
            print(' ')

        print("] ${formatter.format(rate)} $info")
    }

    fun update(value : Double,info : String) {
        if (value < 0.0 || value > max) {
            return
        }

        rate = value / max

        draw(info)

        if (rate == 1.0) {
            print('\n')
        }
    }

    fun reset() {
        rate = 0.0
    }
}
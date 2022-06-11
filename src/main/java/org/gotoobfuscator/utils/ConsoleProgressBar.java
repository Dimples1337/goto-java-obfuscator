package org.gotoobfuscator.utils;

import java.text.DecimalFormat;

/**
 * https://blog.csdn.net/manyu_java/article/details/90760144
 */

@SuppressWarnings("FieldCanBeLocal")
public final class ConsoleProgressBar {
    private final DecimalFormat formatter = new DecimalFormat("#.##%");
    private final int barLen = 10;
    private final char showChar = '=';

    public void show(double value,double max,final String info) {
        if (value < 0.0 || value > max) {
            return;
        }

        reset();

        final double rate = value / max;

        draw(rate,info);

        if (value == max) {
            afterComplete();
        }
    }

    private void draw(final double rate,final String info) {
        System.out.print("|");

        final int len = (int) (rate * barLen);

        for (int i = 0; i < len; i++) {
            System.out.print(showChar);
        }

        for (int i = 0; i < barLen - len; i++) {
            System.out.print(" ");
        }

        System.out.print("| " + format(rate) + " " + info);
    }

    private void reset() {
        System.out.print('\r');
    }

    private void afterComplete() {
        System.out.print('\n');
    }

    private String format(double num) {
        return formatter.format(num);
    }
}

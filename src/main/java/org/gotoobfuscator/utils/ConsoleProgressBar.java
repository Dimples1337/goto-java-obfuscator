package org.gotoobfuscator.utils;

import java.text.DecimalFormat;

/**
 * https://blog.csdn.net/manyu_java/article/details/90760144
 */

@SuppressWarnings("FieldCanBeLocal")
public final class ConsoleProgressBar {
    private final DecimalFormat formatter = new DecimalFormat("#.##%");
    private final int barLen = 10;

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
        System.out.print("[");

        final int len = (int) (rate * barLen);

        for (int i = 0; i < len; i++) {
            System.out.print(color("=",32));
        }

        for (int i = 0; i < barLen - len; i++) {
            System.out.print(color("=",37));
        }

        System.out.print("] " + format(rate) + " " + info);
    }

    @SuppressWarnings("SameParameterValue")
    private String color(String content, int colour) {
        return String.format("\033[%dm%s\033[0m", colour, content);
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

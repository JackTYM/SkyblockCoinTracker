package dev.jacktym.skyblockcointracker.client.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class FormatUtil {
    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));

    public static String formatCoins(long coins) {
        return COMMA_FORMAT.format(coins);
    }

    public static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String formatRate(long gained, int seconds) {
        if (seconds <= 0) return "0";
        long rate = (gained * 3600) / seconds;
        return COMMA_FORMAT.format(rate);
    }
}

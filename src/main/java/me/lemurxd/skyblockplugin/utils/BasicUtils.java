package me.lemurxd.skyblockplugin.utils;

public class BasicUtils {

    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        minutes = minutes % 60;

        return String.format("%d godz. %d min.", hours, minutes);
    }

}

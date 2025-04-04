package com.griefcraft.util;

import java.security.MessageDigest;
import java.util.Formatter;

public class StringUtils {

    /**
     * Parse a time string (e.g 2 hours)
     *
     * @param str
     * @return the result in seconds
     */
    public static long parseTime(String time) {
        long seconds = 0L;

        String[] split = time.split(" ");

        for (int index = 0; index < split.length; index++) {
            if (index == 0) {
                continue;
            }

            String sub = split[index].toLowerCase();
            int multiplier = 0; // e.g 2 hours = 2
            long unit = 0; // e.g hours = 3600

            try {
                multiplier = Integer.parseInt(split[index - 1]);
            } catch (NumberFormatException e) {
                continue;
            }

            // attempt to match the unit time
            if (sub.startsWith("second")) {
                unit = 1;
            } else if (sub.startsWith("minute")) {
                unit = 60;
            } else if (sub.startsWith("hour")) {
                unit = 3600;
            } else if (sub.startsWith("day")) {
                unit = 86400;
            } else if (sub.startsWith("week")) {
                unit = 604800;
            } else if (sub.startsWith("month")) {
                unit = 2629743;
            } else if (sub.startsWith("year")) {
                unit = 31556926;
            } // i do not think decade, century, millenium, etc is necessary beyond here :3

            seconds += multiplier * unit;
        }

        return seconds;
    }

    /**
     * Convert a given time in seconds to a more readable format
     *
     * @param time
     * @return the time in a more readable format (e.g 2 days 5 hours 1 minute 34  seconds)
     */
    public static String timeToString(long time) {
        String str = "";

        if ((System.currentTimeMillis() / 1000L) - time == 0L) {
            return "Not yet known";
        }

        long days = time / 86400;
        time = time - (days * 86400);

        long hours = time / 3600;
        time = time - (hours * 3600);

        long minutes = time / 60;
        time = time - (minutes * 60);

        long seconds = time;

        if (days > 0) {
            str += days + " day" + (days == 1 ? "" : "s") + " ";
        }

        if (hours > 0) {
            str += hours + " hour" + (hours == 1 ? "" : "s") + " ";
        }

        if (minutes > 0) {
            str += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
        }

        if (seconds > 0) {
            str += seconds + " second" + (seconds == 1 ? "" : "s") + " ";
        }

        return str.trim();
    }

    /**
     * Capitalize the first letter in a word
     *
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (str.length() == 0) {
            return str;
        } else if (str.length() == 1) {
            return str.toUpperCase();
        }

        /*
           * First cast it all to lower case and get the char array
           */
        char[] string = str.toLowerCase().toCharArray();

        /*
           * First letter -- capitalize it
           */
        string[0] = Character.toUpperCase(string[0]);

        /*
           * Scan for spaces
           */
        for (int index = 0; index < string.length; index++) {
            if (string[index] == ' ' && index != string.length) {
                /*
                     * convert chars after spaces to upper case
                     */
                string[index + 1] = Character.toUpperCase(string[index + 1]);
            }
        }

        return new String(string);
    }

    /**
     * Encrypt a string using SHA1
     *
     * @param plaintext
     * @return
     */
    public static String encrypt(String plaintext) {
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance("SHA");
            md.update(plaintext.getBytes("UTF-8"));

            final byte[] raw = md.digest();
            return byteArray2Hex(raw);
        } catch (final Exception e) {

        }

        return "";
    }

    /**
     * Check if the command has the correct flag
     *
     * @param args
     * @param checkFlag
     * @return
     */
    public static boolean hasFlag(String[] args, String checkFlag) {
        String flag = args[0].toLowerCase();
        return flag.equals(checkFlag) || flag.equals("-" + checkFlag);
    }

    /**
     * Check if the command has the correct flag
     *
     * @param command
     * @param checkFlag
     * @return
     */
    public static boolean hasFlag(String command, String checkFlag) {
        return command.equals(checkFlag) || command.equals("-" + checkFlag);
    }

    /**
     * Join an array command into a String
     *
     * @param arr
     * @param offset
     * @return
     */
    public static String join(String[] arr, int offset) {
        return join(arr, offset, " ");
    }

    /**
     * Join an array command into a String
     *
     * @param arr
     * @param offset
     * @param delim
     * @return
     */
    public static String join(String[] arr, int offset, String delim) {
        String str = "";

        if (arr == null || arr.length == 0) {
            return str;
        }

        for (int i = offset; i < arr.length; i++) {
            str += arr[i] + delim;
        }

        return str.trim();
    }

    /**
     * Transform a string into one char
     *
     * @param str The string to transform
     * @param chr The char to transform all chars to (ie '*')
     * @return the transformed string
     */
    public static String transform(String str, char chr) {
        final char[] charArray = str.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            charArray[i] = chr;
        }

        return new String(charArray);
    }

    /**
     * Convert a byte array to hex
     *
     * @param hash the hash to convert
     * @return the converted hash
     */
    private static String byteArray2Hex(byte[] hash) {
        final Formatter formatter = new Formatter();
        for (final byte b : hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

}

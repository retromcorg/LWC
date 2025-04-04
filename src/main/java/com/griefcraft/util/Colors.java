package com.griefcraft.util;

import java.util.HashMap;
import java.util.Map;

public class Colors {
    public static final String Black = "\u00A70";

    public static final String Blue = "\u00A73";
    public static final String DarkPurple = "\u00A79";
    public static final String Gold = "\u00A76";
    public static final String Gray = "\u00A78";
    public static final String Green = "\u00A72";
    public static final String LightBlue = "\u00A7b";
    public static final String LightGray = "\u00A77";
    public static final String LightGreen = "\u00A7a";
    public static final String LightPurple = "\u00A7d";
    // contains colors for locales
    public static final Map<String, String> localeColors = new HashMap<String, String>();
    public static final String Navy = "\u00A71";
    public static final String Purple = "\u00A75";
    public static final String Red = "\u00A74";
    public static final String Rose = "\u00A7c";
    public static final String White = "\u00A7f";
    public static final String Yellow = "\u00A7e";

    static {
        localeColors.put("%black%", "\u00A70");
        localeColors.put("%navy%", "\u00A71");
        localeColors.put("%green%", "\u00A72");
        localeColors.put("%blue%", "\u00A73");
        localeColors.put("%red%", "\u00A74");
        localeColors.put("%purple%", "\u00A75");
        localeColors.put("%gold%", "\u00A76");
        localeColors.put("%lighgray%", "\u00A77");
        localeColors.put("%gray%", "\u00A78");
        localeColors.put("%darkpurple%", "\u00A79");
        localeColors.put("%lightgreen%", "\u00A7a");
        localeColors.put("%lightblue%", "\u00A7b");
        localeColors.put("%rose%", "\u00A7c");
        localeColors.put("%lightpurple%", "\u00A7d");
        localeColors.put("%yellow%", "\u00A7e");
        localeColors.put("%white%", "\u00A7f");
    }

}

package com.griefcraft.lwc;

public class LWCInfo {

    /**
     * String value of LWCInfo.VERSION.
     */
    public static String FULL_VERSION;

    public static String VERSION;

    /**
     * Rather than managing the version in multiple spots, I added this method which will be
     * invoked from Plugin startup to set the version, which is pulled from the plugin.yml file.
     *
     * @param version
     * @author morganm
     */
    public static void setVersion(String version) {
        VERSION = version;
        FULL_VERSION = version;
    }
}

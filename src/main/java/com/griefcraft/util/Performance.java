package com.griefcraft.util;

import com.griefcraft.cache.CacheSet;
import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class Performance {

    private static Logger logger = Logger.getLogger("Performance");

    /**
     * Number of queries generated for mem db
     */
    private static int memDBQueries = 0;

    /**
     * Number of queries generated for phys db
     */
    private static int physDBQueries = 0;

    /**
     * Time when LWC was started
     */
    private static long startTime = 0L;

    /**
     * Add a query
     */
    public static void addMemDBQuery() {
        memDBQueries++;
    }

    /**
     * Add a query
     */
    public static void addPhysDBQuery() {
        physDBQueries++;
    }

    /**
     * @return the average amount of queries per second
     */
    public static double getAverage(int queries) {
        return (double) queries / getTimeRunningSeconds();
    }

    /**
     * Generate a new report
     *
     * @return
     */
    public static List<String> generateReport() {
        List<String> report = new ArrayList<String>();
        LWC lwc = LWC.getInstance();
        CacheSet caches = lwc.getCaches();

        report.add(" ");

        report.add(" + Version:\t" + Colors.Gray + LWCInfo.FULL_VERSION);
        report.add(" + Engine:\t" + Colors.Gray + lwc.getPhysicalDatabase().getType());
        report.add(" + Date:\t" + Colors.Gray + new Date());
        report.add(" + Time:\t" + Colors.Gray + getTimeRunningSeconds() + " seconds");
        report.add(" + Players:\t" + Colors.Gray + lwc.getPlugin().getServer().getOnlinePlayers().length);
        report.add(" + Protections:\t" + Colors.Gray + lwc.getPhysicalDatabase().getProtectionCount());
        report.add(" + Cache:\t" + Colors.Gray + caches.getProtections().size() + Colors.Yellow + "/" + Colors.Gray + lwc.getConfiguration().getInt("core.cacheSize", 10000));

        report.add(" ");
        report.add(" - Physical database");
        report.add("  + Queries:\t" + Colors.Gray + physDBQueries);
        report.add("  + Average:\t" + Colors.Gray + getAverage(physDBQueries) + Colors.Yellow + " /second");
        report.add(" ");
        report.add(" - Memory database");
        report.add("  + Queries:\t" + Colors.Gray + memDBQueries);
        report.add("  + Average:\t" + Colors.Gray + getAverage(memDBQueries) + Colors.Yellow + " /second");

        report.add(" ");

        return report;
    }

    /**
     * @return time in seconds it was being ran
     */
    public static int getTimeRunningSeconds() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Initialize
     */
    public static void init() {
        startTime = System.currentTimeMillis();
    }

}

package com.griefcraft.migration;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.AccessRight;
import com.griefcraft.model.History;
import com.griefcraft.model.Protection;
import com.griefcraft.sql.Database.Type;
import com.griefcraft.sql.PhysDB;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

// Sort of just a convenience class, so as to not make the LWC class more cluttered than it is right now
public class MySQLPost200 implements MigrationUtility {

    private static Logger logger = Logger.getLogger("Patcher");

    /**
     * Check for required SQLite->MySQL conversion
     */
    public void run() {
        LWC lwc = LWC.getInstance();
        PhysDB physicalDatabase = lwc.getPhysicalDatabase();

        // this patcher only does something exciting if you have mysql enabled
        // :-)
        if (physicalDatabase.getType() != Type.MySQL) {
            return;
        }

        // this patcher only does something exciting if the old SQLite database
        // still exists :-)
        String database = lwc.getConfiguration().getString("database.path");

        if (database == null || database.equals("")) {
            return;
        }

        File file = new File(database);
        if (!file.exists()) {
            return;
        }

        logger.info("######################################################");
        logger.info("######################################################");
        logger.info("SQLite to MySQL conversion required");

        logger.info("Loading SQLite");

        // rev up those sqlite databases because I sure am hungry for some
        // data...
        PhysDB sqliteDatabase = new PhysDB(Type.SQLite);

        try {
            sqliteDatabase.connect();
            sqliteDatabase.load();

            logger.info("SQLite is good to go");
            physicalDatabase.getConnection().setAutoCommit(false);

            logger.info("Preliminary scan...............");
            int startProtections = physicalDatabase.getProtectionCount();

            int protectionCount = sqliteDatabase.getProtectionCount();
            int rightsCount = sqliteDatabase.getRightsCount();
            int historyCount = sqliteDatabase.getHistoryCount();

            int expectedProtections = protectionCount + startProtections;

            logger.info("TO CONVERT:");
            logger.info("Protections:\t" + protectionCount);
            logger.info("Rights:\t\t" + rightsCount);
            logger.info("History:\t" + historyCount);
            logger.info("");

            if (protectionCount > 0) {
                logger.info("Converting: PROTECTIONS");

                List<Protection> tmp = sqliteDatabase.loadProtections();

                for (Protection protection : tmp) {
                    int x = protection.getX();
                    int y = protection.getY();
                    int z = protection.getZ();

                    // register it
                    physicalDatabase.registerProtection(protection.getBlockId(), protection.getType(), protection.getWorld(), protection.getOwner(), protection.getData(), x, y, z);

                    // get the new protection, to retrieve the id
                    Protection registered = physicalDatabase.loadProtection(protection.getWorld(), x, y, z);

                    // get the rights in the world
                    List<AccessRight> tmpRights = sqliteDatabase.loadRights(protection.getId());

                    // register the new rights using the newly registered protection
                    for (AccessRight right : tmpRights) {
                        physicalDatabase.registerProtectionRights(registered.getId(), right.getName(), right.getRights(), right.getType());
                    }
                }

                logger.info("COMMITTING");
                physicalDatabase.getConnection().commit();
                logger.info("OK , expecting: " + expectedProtections);
                if (expectedProtections == (protectionCount = physicalDatabase.getProtectionCount())) {
                    logger.info("OK.");
                } else {
                    logger.info("Weird, only " + protectionCount + " protections are in the database? Continuing...");
                }
            }

            if(historyCount > 0) {
                logger.info("Converting: HISTORY");

                List<History> tmp = sqliteDatabase.loadHistory();

                for(History history : tmp) {
                    // make sure it's assumed it does not exist in the database
                    history.setExists(false);

                    // sync the history object with the active database (ala MySQL)
                    history.sync();
                }

                logger.info("OK");
            }

            logger.info("Closing SQLite");
            sqliteDatabase.getConnection().close();

            logger.info("Renaming \"" + database + "\" to \"" + database + ".old\"");
            if (!file.renameTo(new File(database + ".old"))) {
                logger.info("NOTICE: FAILED TO RENAME lwc.db!! Please rename this manually!");
            }

            logger.info("SQLite to MySQL conversion is now complete!\n");
            logger.info("Thank you!");
        } catch (Exception e) {
            logger.info("#### SEVERE ERROR: Something bad happened when converting the database (Oops!)");
            e.printStackTrace();
        }

        try {
            physicalDatabase.getConnection().setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.info("######################################################");
        logger.info("######################################################");
    }

}

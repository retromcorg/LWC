package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.sql.Database;
import com.griefcraft.util.Colors;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AdminCleanup extends JavaModule {

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("a", "admin")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!args[0].equals("cleanup")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        // if we shouldn't output
        boolean silent = false;

        if (args.length > 1 && args[1].equalsIgnoreCase("silent")) {
            silent = true;
        }

        lwc.sendLocale(sender, "protection.admin.cleanup.start", "count", lwc.getPhysicalDatabase().getProtectionCount());

        // do the work in a separate thread so we don't fully lock the server
        // new Thread(new Admin_Cleanup_Thread(lwc, sender)).start();
        new Admin_Cleanup_Thread(lwc, sender, silent).run();
        return;
    }

    /**
     * Class that handles cleaning up the LWC database usage: /lwc admin cleanup
     */
    private static class Admin_Cleanup_Thread implements Runnable {

        private LWC lwc;
        private CommandSender sender;
        private boolean silent;

        public Admin_Cleanup_Thread(LWC lwc, CommandSender sender, boolean silent) {
            this.lwc = lwc;
            this.sender = sender;
            this.silent = silent;
        }

        /**
         * Push removal changes to the database
         *
         * @param toRemove
         */
        public void push(List<Integer> toRemove) throws SQLException {
            final StringBuilder builder = new StringBuilder();
            final int total = toRemove.size();
            int count = 0;

            // iterate over the items to remove
            Iterator<Integer> iter = toRemove.iterator();

            // the database prefix
            String prefix = lwc.getPhysicalDatabase().getPrefix();

            // create the statement to use
            Statement statement = lwc.getPhysicalDatabase().getConnection().createStatement();

            while (iter.hasNext()) {
                int protectionId = iter.next();

                if (count % 100000 == 0) {
                    builder.append("DELETE FROM " + prefix + "protections WHERE id IN (" + protectionId);
                } else {
                    builder.append("," + protectionId);
                }

                if (count % 100000 == 99999 || count == (total - 1)) {
                    builder.append(")");
                    statement.executeUpdate(builder.toString());
                    builder.setLength(0);

                    sender.sendMessage(Colors.Green + "REMOVED " + (count + 1) + " / " + total);
                }

                count++;
            }

            statement.close();
        }

        public void run() {
            long start = System.currentTimeMillis();
            int completed = 0;
            int count = 0;

            List<Integer> toRemove = new LinkedList<Integer>();
            int totalProtections = lwc.getPhysicalDatabase().getProtectionCount();

            sender.sendMessage("Loading protections via STREAM mode");

            try {
                Statement resultStatement = lwc.getPhysicalDatabase().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

                if(lwc.getPhysicalDatabase().getType() == Database.Type.MySQL) {
                    resultStatement.setFetchSize(Integer.MIN_VALUE);
                }

                String prefix = lwc.getPhysicalDatabase().getPrefix();
                ResultSet result = resultStatement.executeQuery("SELECT " + prefix + "protections.id AS protectionId, " + prefix + "protections.type AS protectionType, x, y, z, flags, blockId, world, owner, password, date, last_accessed FROM " + prefix + "protections");

                while (result.next()) {
                    Protection protection = lwc.getPhysicalDatabase().resolveProtectionNoRights(result);
                    World world = protection.getBukkitWorld();

                    count++;

                    if (count % 100000 == 0 || count == totalProtections || count == 1) {
                        sender.sendMessage(Colors.Red + count + " / " + totalProtections);
                    }

                    if (world == null) {
                        if (!silent) {
                            lwc.sendLocale(sender, "protection.admin.cleanup.noworld", "world", protection.getWorld());
                        }

                        continue;
                    }

                    // now we can check the world for the protection
                    Block block = protection.getBlock();

                    // remove protections not found in the world
                    if (block == null || !lwc.isProtectable(block)) {
                        toRemove.add(protection.getId());
                        completed++;

                        if (!silent) {
                            lwc.sendLocale(sender, "protection.admin.cleanup.removednoexist", "protection", protection.toString());
                        }
                    }
                }

                // Close the streaming statement
                result.close();
                resultStatement.close();

                // flush all of the queries
                push(toRemove);
            } catch (Exception e) {
                sender.sendMessage("Uh-oh, something bad happened while cleaning up the LWC database!");
                lwc.sendLocale(sender, "protection.internalerror", "id", "cleanup");
                e.printStackTrace();
            }

            long finish = System.currentTimeMillis();
            float timeInSeconds = (finish - start) / 1000.0f;

            lwc.sendLocale(sender, "protection.admin.cleanup.complete", "count", completed, "seconds", timeInSeconds);
        }

        private List<Protection> getAll(List<Protection> protections, String world, int x, int y, int z) {
            List<Protection> tmp = new ArrayList<Protection>();

            for (Protection protection : protections) {
                if (protection.getWorld() != null && world != null && protection.getWorld().equals(world)) {
                    if (protection.getX() == x && protection.getY() == y && protection.getZ() == z) {
                        tmp.add(protection);
                    }
                }
            }

            return tmp;
        }

    }

}
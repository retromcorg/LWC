package com.griefcraft.modules.flag;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.util.config.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MagnetModule extends JavaModule {

    private Configuration configuration = Configuration.load("magnet.yml");

    /**
     * If this module is enabled
     */
    private boolean enabled = false;

    /**
     * The item blacklist
     */
    private List<Integer> itemBlacklist;

    /**
     * The radius around the container in which to suck up items
     */
    private int radius;

    /**
     * How many items to check each time
     */
    private int perSweep;

    /**
     * The current entity queue
     */
    private List<Entity> entities = null;

    // does all of the work
    // searches the worlds for items and magnet chests nearby
    private class MagnetTask implements Runnable {
        public void run() {
            Server server = Bukkit.getServer();
            LWC lwc = LWC.getInstance();

            for (World world : server.getWorlds()) {
                String worldName = world.getName();

                if (entities == null || entities.size() == 0) {
                    entities = world.getEntities();
                }

                Iterator<Entity> iterator = entities.iterator();
                int count = 0;

                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    if (!(entity instanceof Item)) {
                        iterator.remove();
                        continue;
                    }

                    if (count > perSweep) {
                        break;
                    }

                    Item item = (Item) entity;
                    ItemStack itemStack = item.getItemStack();

                    // check if it is in the blacklist
                    if (itemBlacklist.contains(itemStack.getTypeId())) {
                        iterator.remove();
                        continue;
                    }

                    Location location = item.getLocation();
                    int x = location.getBlockX();
                    int y = location.getBlockY();
                    int z = location.getBlockZ();

                    List<Protection> protections = lwc.getPhysicalDatabase().loadProtections(worldName, x, y, z, radius);
                    Block block;
                    Protection protection;

                    for (Protection temp : protections) {
                        protection = temp;
                        block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());

                        // we only want inventory blocks
                        if (!(block.getState() instanceof ContainerBlock)) {
                            continue;
                        }

                        if (!protection.hasFlag(Protection.Flag.MAGNET)) {
                            continue;
                        }

                        // Remove the items and suck them up :3
                        Map<Integer, ItemStack> remaining = lwc.depositItems(block, itemStack);

                        if (remaining.size() == 1) {
                            ItemStack other = remaining.values().iterator().next();

                            if (itemStack.getTypeId() == other.getTypeId() && itemStack.getAmount() == other.getAmount()) {
                                continue;
                            }
                        }

                        // remove the item on the ground
                        item.remove();

                        // if we have a remainder, we need to drop them
                        if (remaining.size() > 0) {
                            for (ItemStack stack : remaining.values()) {
                                world.dropItemNaturally(location, stack);
                            }
                        }

                        break;
                    }

                    count++;
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void load(LWC lwc) {
        enabled = configuration.getBoolean("magnet.enabled", false);
        itemBlacklist = new ArrayList<Integer>();
        radius = configuration.getInt("magnet.radius", 3);
        perSweep = configuration.getInt("magnet.perSweep", 20);

        if (!enabled) {
            return;
        }

        // get the item blacklist
        List<String> temp = configuration.getStringList("magnet.blacklist", new ArrayList<String>());

        for (String item : temp) {
            Material material = Material.matchMaterial(item);

            if (material != null) {
                itemBlacklist.add(material.getId());
            }
        }

        // register our search thread schedule
        MagnetTask searchThread = new MagnetTask();
        lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), searchThread, 50, 50);
    }

}

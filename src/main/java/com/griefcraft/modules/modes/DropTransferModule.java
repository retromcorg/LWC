package com.griefcraft.modules.modes;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class DropTransferModule extends JavaModule {

    private LWC lwc;

    @Override
    public void load(LWC lwc) {
        this.lwc = lwc;
    }

    /**
     * Check if the player is currently drop transferring
     *
     * @param player
     * @return
     */
    private boolean isPlayerDropTransferring(String player) {
        return lwc.getMemoryDatabase().hasMode(player, "+dropTransfer");
    }

    /**
     * Get the drop transfer target for a player
     *
     * @param player
     * @return
     */
    private int getPlayerDropTransferTarget(String player) {
        String target = lwc.getMemoryDatabase().getModeData(player, "dropTransfer");

        try {
            return Integer.parseInt(target);
        } catch (NumberFormatException e) {
        }

        return -1;
    }

    @Override
    public Result onDropItem(LWC lwc, Player player, Item item, ItemStack itemStack) {
        int protectionId = getPlayerDropTransferTarget(player.getName());

        if (protectionId == -1) {
            return DEFAULT;
        }

        if (!isPlayerDropTransferring(player.getName())) {
            return DEFAULT;
        }

        Protection protection = lwc.getPhysicalDatabase().loadProtection(protectionId);

        if (protection == null) {
            player.sendMessage(Colors.Red + "Protection no longer exists");
            lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
            return DEFAULT;
        }

        // load the world and the inventory
        World world = player.getServer().getWorld(protection.getWorld());

        if (world == null) {
            player.sendMessage(Colors.Red + "Invalid world!");
            lwc.getMemoryDatabase().unregisterMode(player.getName(), "dropTransfer");
            return DEFAULT;
        }

        Block block = world.getBlockAt(protection.getX(), protection.getY(), protection.getZ());
        Map<Integer, ItemStack> remaining = lwc.depositItems(block, itemStack);

        if (remaining.size() > 0) {
            player.sendMessage("Chest could not hold all the items! Have the remaining items back.");

            for (ItemStack temp : remaining.values()) {
                player.getInventory().addItem(temp);
            }
        }
        player.updateInventory(); // if they're in the chest and dropping items, this is required
        item.remove();

        return DEFAULT;
    }

    @Override
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin) {
        if (!actions.contains("dropTransferSelect")) {
            return DEFAULT;
        }

        if (!canAccess) {
            lwc.sendLocale(player, "protection.interact.dropxfer.noaccess");
        } else {
            if (protection.getBlockId() != Material.CHEST.getId()) {
                lwc.sendLocale(player, "protection.interact.dropxfer.notchest");
                lwc.getMemoryDatabase().unregisterAllActions(player.getName());
                return CANCEL;
            }

            lwc.getMemoryDatabase().registerMode(player.getName(), "dropTransfer", protection.getId() + "");
            lwc.getMemoryDatabase().registerMode(player.getName(), "+dropTransfer");
            lwc.sendLocale(player, "protection.interact.dropxfer.finalize");
        }

        lwc.getMemoryDatabase().unregisterAllActions(player.getName()); // ignore the persist mode
        return DEFAULT;
    }

    @Override
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions) {
        if (!actions.contains("dropTransferSelect")) {
            return DEFAULT;
        }

        lwc.sendLocale(player, "protection.interact.dropxfer.notprotected");
        lwc.getMemoryDatabase().unregisterAllActions(player.getName());

        return DEFAULT;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (!event.hasFlag("p", "mode")) {
            return;
        }

        if(event.isCancelled()) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        Player player = (Player) sender;
        String mode = args[0].toLowerCase();

        if (!mode.equals("droptransfer")) {
            return;
        }

        event.setCancelled(true);

        // internal name
        mode = "dropTransfer";

        if (args.length < 2) {
            lwc.sendLocale(player, "protection.modes.dropxfer.help");
            return;
        }

        String action = args[1].toLowerCase();
        String playerName = player.getName();

        if (action.equals("select")) {
            if (isPlayerDropTransferring(playerName)) {
                lwc.sendLocale(player, "protection.modes.dropxfer.select.error");
                return;
            }

            lwc.getMemoryDatabase().unregisterMode(playerName, mode);
            lwc.getMemoryDatabase().registerAction("dropTransferSelect", playerName, "");

            lwc.sendLocale(player, "protection.modes.dropxfer.select.finalize");
        } else if (action.equals("on")) {
            int target = getPlayerDropTransferTarget(playerName);

            if (target == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
                return;
            }

            lwc.getMemoryDatabase().registerMode(playerName, "+dropTransfer");
            lwc.sendLocale(player, "protection.modes.dropxfer.on.finalize");
        } else if (action.equals("off")) {
            int target = getPlayerDropTransferTarget(playerName);

            if (target == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.selectchest");
                return;
            }

            lwc.getMemoryDatabase().unregisterMode(playerName, "+dropTransfer");
            lwc.sendLocale(player, "protection.modes.dropxfer.off.finalize");
        } else if (action.equals("status")) {
            if (getPlayerDropTransferTarget(playerName) == -1) {
                lwc.sendLocale(player, "protection.modes.dropxfer.status.off");
            } else {
                if (isPlayerDropTransferring(playerName)) {
                    lwc.sendLocale(player, "protection.modes.dropxfer.status.active");
                } else {
                    lwc.sendLocale(player, "protection.modes.dropxfer.status.inactive");
                }
            }
        }

        return;
    }

}

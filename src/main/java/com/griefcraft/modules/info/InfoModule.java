package com.griefcraft.modules.info;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoModule extends JavaModule {

    @Override
    public void onProtectionInteract(LWCProtectionInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("info")) {
            return;
        }

        LWC lwc = event.getLWC();
        Protection protection = event.getProtection();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.info.finalize", "type", lwc.getLocale(protection.typeToString().toLowerCase()), "owner", protection.getOwner(), "access", lwc.getLocale((event.canAccess() ? "yes" : "no")));

        if (lwc.isAdmin(player)) {
            lwc.sendLocale(player, "protection.interact.info.raw", "raw", protection.toString());
        }

        lwc.removeModes(player);
        return;
    }

    @Override
    public void onBlockInteract(LWCBlockInteractEvent event) {
        if (event.getResult() != Result.DEFAULT) {
            return;
        }

        if (!event.hasAction("info")) {
            return;
        }

        LWC lwc = event.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();
        event.setResult(Result.CANCEL);

        lwc.sendLocale(player, "protection.interact.error.notregistered", "block", LWC.materialToString(block));
        lwc.removeModes(player);
        return;
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("i", "info")) {
            return;
        }

        LWC lwc = event.getLWC();
        CommandSender sender = event.getSender();
        String[] args = event.getArgs();

        if (!(sender instanceof Player)) {
            return;
        }

        event.setCancelled(true);

        if (!lwc.hasPlayerPermission(sender, "lwc.info")) {
            lwc.sendLocale(sender, "protection.accessdenied");
            return;
        }

        Player player = (Player) sender;
        String type = "info";

        if (args.length > 0) {
            type = args[0].toLowerCase();
        }

        if (type.equals("info")) {
            lwc.getMemoryDatabase().unregisterAllActions(player.getName());
            lwc.getMemoryDatabase().registerAction("info", player.getName());
            lwc.sendLocale(player, "protection.info.finalize");
        }

        return;
    }

}

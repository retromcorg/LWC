package com.griefcraft.listeners;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import com.griefcraft.model.ProtectionTypes;
import com.griefcraft.scripting.Module.Result;
import com.griefcraft.scripting.ModuleLoader.Event;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.*;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

import java.util.*;

public class LWCBlockListener extends BlockListener {

    /**
     * The plugin instance
     */
    private LWCPlugin plugin;
    private Set<Material> attachmentsTopToCheck = new HashSet<>(
        Arrays.asList(
            Material.SAPLING,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.LONG_GRASS,
            Material.DEAD_BUSH,
            Material.YELLOW_FLOWER,
            Material.RED_ROSE,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.TORCH,
            Material.FIRE,
            Material.REDSTONE_WIRE,
            Material.SIGN_POST,
            Material.WOODEN_DOOR,
            Material.RAILS,
            Material.LEVER,
            Material.STONE_PLATE,
            Material.IRON_DOOR_BLOCK,
            Material.WOOD_PLATE,
            Material.REDSTONE_TORCH_OFF,
            Material.REDSTONE_TORCH_ON,
            Material.SNOW,
            Material.CACTUS,
            Material.SUGAR_CANE_BLOCK,
            Material.CAKE_BLOCK
        )
    );

    public LWCBlockListener(LWCPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        Result result = lwc.getModuleLoader().dispatchEvent(Event.REDSTONE, protection, block, event.getOldCurrent());
        LWCRedstoneEvent evt = new LWCRedstoneEvent(event, protection);
        lwc.getModuleLoader().dispatchEvent(evt);

        if (evt.isCancelled() || result == Result.CANCEL) {
            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block == null) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);

        if (!canAccess) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        boolean ignoreBlockDestruction = Boolean.parseBoolean(lwc.resolveProtectionConfiguration(material, "ignoreBlockDestruction"));

        if (ignoreBlockDestruction) {
            return;
        }

        Protection protection = lwc.findProtection(block);

        if (protection == null) {
            return;
        }

        boolean canAccess = lwc.canAccessProtection(player, protection);
        boolean canAdmin = lwc.canAdminProtection(player, protection);

        try {
            Result result = lwc.getModuleLoader().dispatchEvent(Event.DESTROY_PROTECTION, player, protection, block, canAccess, canAdmin);
            LWCProtectionDestroyEvent evt = new LWCProtectionDestroyEvent(player, protection, canAccess, canAdmin);
            lwc.getModuleLoader().dispatchEvent(evt);

            if (evt.isCancelled() || result == Result.CANCEL) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            event.setCancelled(true);
            lwc.sendLocale(player, "protection.internalerror", "id", "BLOCK_BREAK");
            e.printStackTrace();
        }
    }

    @Override
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        if (event.getBlock().getType() != Material.SAND && event.getBlock().getType() != Material.GRAVEL) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block upNeighbor = event.getBlock().getRelative(BlockFace.UP);
        if (attachmentsTopToCheck.contains(upNeighbor.getType()) && lwc.findProtection(upNeighbor) != null) {
            event.setCancelled(true);

            return;
        }

        List<BlockFace> horizontals = Arrays.asList(
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
        );
        for (BlockFace face: horizontals) {
            Block neighbor = event.getBlock().getRelative(face);
            if (neighbor.getState() != null && (neighbor.getState().getData() instanceof Attachable)) {
                Attachable data = (Attachable) neighbor.getState().getData();
                if (data.getAttachedFace() == face.getOppositeFace() && lwc.findProtection(neighbor) != null) {
                    event.setCancelled(true);

                    return;
                }
            }
        }
    }

    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Block piston = event.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();

        if(!(data instanceof PistonBaseMaterial)) {
            return;
        }

        BlockFace direction = ((PistonBaseMaterial) data).getFacing();
        Block block = event.getBlock().getRelative(direction);

        Protection protection = lwc.findProtection(block);

        if (protection != null) {
            event.setCancelled(true);
        }
    }

    /**
     * Used for auto registering placed protections
     */
    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!LWC.ENABLED) {
            return;
        }

        LWC lwc = plugin.getLWC();
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // water exploit (placing 3+ chests in a row inside water, fire, etc)
        if (block.getType() == Material.CHEST) {
            if (lwc.findAdjacentDoubleChest(block) != null) {
                event.setCancelled(true);
                return;
            }
        }

        // The placable block must be protectable
        if (!lwc.isProtectable(block)) {
            return;
        }

        String autoRegisterType = plugin.getLWC().resolveProtectionConfiguration(block.getType(), "autoRegister");

        // is it auto registerable?
        if (!autoRegisterType.equalsIgnoreCase("private") && !autoRegisterType.equalsIgnoreCase("public")) {
            return;
        }

        if (!lwc.hasPermission(player, "lwc.create." + autoRegisterType, "lwc.create", "lwc.protect")) {
            return;
        }

        // default to public
        int type = ProtectionTypes.PUBLIC;

        if (autoRegisterType.equalsIgnoreCase("private")) {
            type = ProtectionTypes.PRIVATE;
        }

        // If it's a chest, make sure they aren't placing it beside an already registered chest
        if (block.getType() == Material.CHEST) {
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

            for (BlockFace blockFace : faces) {
                Block face = block.getRelative(blockFace);

                //They're placing it beside a chest, check if it's already protected
                if (face.getType() == Material.CHEST) {
                    if (lwc.getPhysicalDatabase().loadProtection(face.getWorld().getName(), face.getX(), face.getY(), face.getZ()) != null) {
                        return;
                    }
                }
            }
        }

        try {
            Result registerProtection = lwc.getModuleLoader().dispatchEvent(Event.REGISTER_PROTECTION, player, block);
            LWCProtectionRegisterEvent evt = new LWCProtectionRegisterEvent(player, block);
            lwc.getModuleLoader().dispatchEvent(evt);

            // something cancelled registration
            if (evt.isCancelled() || registerProtection == Result.CANCEL) {
                return;
            }

            // All good!
            Protection protection = lwc.getPhysicalDatabase().registerProtection(block.getTypeId(), type, block.getWorld().getName(), player.getName(), "", block.getX(), block.getY(), block.getZ());
            lwc.sendLocale(player, "protection.onplace.create.finalize", "type", lwc.getLocale(autoRegisterType.toLowerCase()), "block", LWC.materialToString(block));

            if (protection != null) {
                lwc.getModuleLoader().dispatchEvent(Event.POST_REGISTRATION, protection);
                lwc.getModuleLoader().dispatchEvent(new LWCProtectionRegistrationPostEvent(protection));
            }
        } catch (Exception e) {
            lwc.sendLocale(player, "protection.internalerror", "id", "PLAYER_INTERACT");
            e.printStackTrace();
        }
    }

}

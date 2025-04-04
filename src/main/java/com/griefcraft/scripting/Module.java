package com.griefcraft.scripting;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.event.LWCAccessEvent;
import com.griefcraft.scripting.event.LWCBlockInteractEvent;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.scripting.event.LWCDropItemEvent;
import com.griefcraft.scripting.event.LWCProtectionDestroyEvent;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.scripting.event.LWCProtectionRegisterEvent;
import com.griefcraft.scripting.event.LWCProtectionRegistrationPostEvent;
import com.griefcraft.scripting.event.LWCProtectionRemovePostEvent;
import com.griefcraft.scripting.event.LWCRedstoneEvent;
import com.griefcraft.scripting.event.LWCSendLocaleEvent;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This interface defines methods that modules may implement
 */
public interface Module {

    // Results returned by methods
    public enum Result {
        ALLOW, CANCEL, DEFAULT
    }

    /**
     * Called when the module is loaded
     */
    public void load(LWC lwc);

    /**
     * Find out the access level of a player to a protection
     *
     * @param event
     */
    public void protectionAccessRequest(LWCAccessEvent event);

    /**
     * Called when a player drops an item
     *
     * @param event
     */
    public void onDropItem(LWCDropItemEvent event);

    /**
     * Called when a player or console executes an LWC command
     *
     * @param event
     */
    public void onCommand(LWCCommandEvent event);

    /**
     * Called when redstone interacts with a protection
     *
     * @param event
     */
    public void onRedstone(LWCRedstoneEvent event);

    /**
     * Called when a protection is destroyed
     *
     * @param event
     */
    public void onDestroyProtection(LWCProtectionDestroyEvent event);

    /**
     * Called when a valid protection is interacted with
     *
     * @param event
     */
    public void onProtectionInteract(LWCProtectionInteractEvent event);

    /**
     * Called when an unprotected block is interacted with
     *
     * @param event
     */
    public void onBlockInteract(LWCBlockInteractEvent event);

    /**
     * Called immediately before a protection is registered
     *
     * @param event
     */
    public void onRegisterProtection(LWCProtectionRegisterEvent event);

    /**
     * Called after a protection is registered
     *
     * @param event
     */
    public void onPostRegistration(LWCProtectionRegistrationPostEvent event);

    /**
     * Called after a protection is removed (the Protection class given is immutable.)
     *
     * @param event
     */
    public void onPostRemoval(LWCProtectionRemovePostEvent event);

    /**
     * Called when LWC or another module sends a locale message to a player
     *
     * @param event
     */
    public void onSendLocale(LWCSendLocaleEvent event);

    /**
     * See if a player can access a protection
     *
     * @param lwc
     * @param player
     * @param protection
     * @return
     */
    @Deprecated
    public Result canAccessProtection(LWC lwc, Player player, Protection protection);

    /**
     * See if a player can administrate a protection (i.e modify it)
     *
     * @param lwc
     * @param player
     * @param protection
     * @return
     */
    @Deprecated
    public Result canAdminProtection(LWC lwc, Player player, Protection protection);

    /**
     * Called when a player drops an item
     *
     * @param lwc
     * @param player
     * @param item
     * @param itemStack
     * @return
     */
    @Deprecated
    public Result onDropItem(LWC lwc, Player player, Item item, ItemStack itemStack);

    /**
     * Player or console command
     *
     * @param lwc
     * @param sender
     * @param command does not include "lwc", eg. /lwc info = "info"
     * @param args
     * @return
     */
    @Deprecated
    public Result onCommand(LWC lwc, CommandSender sender, String command, String[] args);

    /**
     * Called when redstone is passed to a valid protection
     *
     * @param lwc
     * @param protection
     * @param block
     * @param current    the old current
     * @return
     */
    @Deprecated
    public Result onRedstone(LWC lwc, Protection protection, Block block, int current);

    /**
     * Called when a protection is destroyed
     *
     * @param lwc
     * @param protection
     * @param block
     * @return
     */
    @Deprecated
    public Result onDestroyProtection(LWC lwc, Player player, Protection protection, Block block, boolean canAccess, boolean canAdmin);

    /**
     * Called when a player left interacts with a valid protection
     *
     * @param lwc
     * @param player
     * @param protection
     * @param canAccess
     * @return
     */
    @Deprecated
    public Result onProtectionInteract(LWC lwc, Player player, Protection protection, List<String> actions, boolean canAccess, boolean canAdmin);

    /**
     * Called when a player interacts with a block
     *
     * @param lwc
     * @param player
     * @param block
     * @param actions
     * @return
     */
    @Deprecated
    public Result onBlockInteract(LWC lwc, Player player, Block block, List<String> actions);

    /**
     * Called just before a protection registration is finalized, after all post-checks are passed.
     *
     * @param lwc
     * @param player
     * @param block
     * @return
     */
    @Deprecated
    public Result onRegisterProtection(LWC lwc, Player player, Block block);

    /**
     * Called after a protection is registered
     *
     * @param lwc
     * @param protection
     */
    @Deprecated
    public void onPostRegistration(LWC lwc, Protection protection);

    /**
     * Called after a protection is removed (at this point, the protection is IMMUTABLE.)
     *
     * @param lwc
     * @param protection
     */
    @Deprecated
    public void onPostRemoval(LWC lwc, Protection protection);

    /**
     * Called when a localized message is sent to a player (e.g lwc.accessdenied)
     *
     * @param lwc
     * @param player
     * @param locale
     * @since LWC 3.40
     */
    @Deprecated
    public Result onSendLocale(LWC lwc, Player player, String locale);

}

package com.griefcraft.integration.permissions;

import com.griefcraft.integration.IPermissions;
import com.griefcraft.lwc.LWC;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class SuperPermsPermissions implements IPermissions {

    /**
     * The group prefix to use to lookup in Permissions - can be overrided in core.yml with groupPrefix: 'new.prefix.'
     * Must include leading period (.)
     * 
     * Default: lwc.group.
     */
    private String groupPrefix;

    public SuperPermsPermissions() {
        groupPrefix = LWC.getInstance().getConfiguration().getString("core.groupPrefix", "lwc.group.");
    }

    public boolean isActive() {
        return true;
    }

    public boolean permission(Player player, String node) {
        return player.hasPermission(node);
    }

    // modified implementation by ZerothAngel ( https://github.com/Hidendra/LWC/issues/88#issuecomment-2017807 )
    public List<String> getGroups(Player player) {
        LWC lwc = LWC.getInstance();
        // Haters are gonna hate, yo
        if (lwc.getRemoveMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose() != null) {
            return lwc.getRemoveMeAndRemoveNijiPermissionsButIfItIsRemovedAllHellBreaksLoose().getGroups(player);
        }

        List<String> groups = new ArrayList<String>();
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if(pai.getPermission().startsWith(groupPrefix)) {
                groups.add(pai.getPermission().substring(groupPrefix.length()));
            }
        }

        return groups;
    }

}

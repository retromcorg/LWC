package com.griefcraft.modules.admin;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCInfo;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;

public class AdminVersion extends JavaModule {

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

        if (!args[0].equals("version")) {
            return;
        }

        // we have the right command
        event.setCancelled(true);

        String pluginColor = Colors.Green;
        String full = LWCInfo.FULL_VERSION;

        lwc.sendLocale(sender, "protection.admin.version.finalize", "plugin_color", pluginColor, "plugin_version", full);
        return;
    }

}

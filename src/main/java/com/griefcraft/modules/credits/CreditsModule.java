package com.griefcraft.modules.credits;

import com.griefcraft.lwc.LWC;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCCommandEvent;
import com.griefcraft.util.Colors;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CreditsModule extends JavaModule {

    /**
     * How many lines to send when they first use /lwc credits
     */
    private static final int FIRST_SEND = 2;

    /**
     * How often to send messages
     */
    private static final long UPDATE_INTERVAL = 1150L;

    /**
     * The credits list
     */
    private String[] credits;

    /**
     * Players to send to and the credits index
     */
    private final Map<CommandSender, Integer> scrolling = Collections.synchronizedMap(new HashMap<CommandSender, Integer>());

    private class CreditsTask implements Runnable {

        public void run() {
            while (LWC.ENABLED) {
                synchronized (scrolling) {
                    Iterator<Map.Entry<CommandSender, Integer>> iter = scrolling.entrySet().iterator();

                    while (iter.hasNext()) {
                        Map.Entry<CommandSender, Integer> entry = iter.next();
                        CommandSender sender = entry.getKey();
                        int index = entry.getValue();

                        // Done!
                        if (index >= credits.length) {
                            iter.remove();
                            continue;
                        }

                        // if they're a player, and not online, don't send
                        if ((sender instanceof Player) && !((Player) sender).isOnline()) {
                            iter.remove();
                            continue;
                        }

                        // if it's 0, we should bulk send the first few
                        if (index == 0) {
                            for (int i = 0; i < FIRST_SEND; i++) {
                                if (index >= credits.length) {
                                    break;
                                }

                                sender.sendMessage(credits[index]);
                                index++;
                            }
                        } else {
                            sender.sendMessage(credits[index]);
                            index++;
                        }

                        // update the index
                        entry.setValue(index);
                    }
                }

                try {
                    Thread.sleep(UPDATE_INTERVAL);
                } catch (Exception e) {
                }
            }
        }

    }

    @Override
    public void load(LWC lwc) {
        credits = new String[]{
                Colors.Green + "LWC, a Protection mod developed by Hidendra ....",
                "Serving Minecraft loyally since September 2010 ....",
                " ",

                Colors.Red + "Core contributions",
                "angelsl",
                "morganm",
                " ",

                Colors.Red + "Translations",
                Colors.Green + "German",
                "Dawodo",
                " ",

                Colors.Green + "Polish",
                "Geoning",
                "dudsonowa",
                "andrewkm",
                " ",

                Colors.Green + "French",
                "cehel",
                " ",

                Colors.Green + "Dutch",
                "Madzero",
                "aoa2003",
                " ",

                Colors.Green + "Czech",
                "hofec",
                " ",

                Colors.Green + "Swedish",
                "christley",
                " ",

                Colors.Green + "Russian",
                "IlyaGulya",
                " ",

                Colors.Green + "Spanish",
                "Raul \"RME\" Martinez",
                "David \"DME\" Martinez",
                " ",

                Colors.Green + "Danish",
                "TCarlsen",
                " ",

                Colors.Red + "Donations",
                Colors.Gray + "(chronological order)",
                "darknavi",
                "Vetyver",
                "pablo0713",
                "IrishSailor & Land of Legend server" + Colors.Red + " X2",
                "aidan",
                "MonsterTKE" + Colors.Red + " X2",
                "wokka",
                "Andreoli3",
                " ",

                Colors.Red + "And....",
                Colors.LightBlue + "Old Griefcraft server -- love you guys!",
                "jobsti",
                "Joy",
                "KaneHart",
                "Kainzo (you find issues before I have a chance to look :3)",
                "& the Herocraft team",
                "#bukkit",
                "Bryan (CursedChild)",
                "Ken (i_pk_pjers_i)",
                "SeeD419",
                "Lim-Dul",
                "arensirb",
                "RustyDagger",
                "HotelErotica",
                "andrewkm",
                "Moo0",
                "Dawodo",
                "xPaw",
                "Samkio",
                "msleeper",
                "Taco",
                "Acrobat",
                "SquallSeeD31",
                "Wahrheit",
                "Kerazene",
                "spunkiie",
                "Zalastri",
                " ",

                Colors.Yellow + "To everyone else and anyone I missed....",
                "LWC would not be the awesome plugin it is today if not also for those not listed",
                " ",
                Colors.Blue + "THANK YOU!"
        };

        // not using the bukkit scheduler because tick rates vary from server to server
        // on one server, it'll send every second, while on another, every 5 seconds!
        CreditsTask task = new CreditsTask();
        new Thread(task).start();
        // lwc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(lwc.getPlugin(), task, 10, 10);
    }

    @Override
    public void onCommand(LWCCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!event.hasFlag("credits", "thanks")) {
            return;
        }

        CommandSender sender = event.getSender();

        if (!scrolling.containsKey(sender)) {
            scrolling.put(sender, 0);
        } else {
            scrolling.remove(sender);
        }

        event.setCancelled(true);
        return;
    }

}

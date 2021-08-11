package com.carpour.logger.Events;

import com.carpour.logger.Discord.Discord;
import com.carpour.logger.Main;
import com.carpour.logger.Utils.FileHandler;
import com.carpour.logger.MySQL.MySQLData;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class onBlockPlace implements Listener {

    private final Main main = Main.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)

    public void onPlace(BlockPlaceEvent event) {

        Player player = event.getPlayer();
        String playername = player.getName();
        World world = player.getWorld();
        String worldname = world.getName();
        int x = event.getBlock().getLocation().getBlockX();
        int y = event.getBlock().getLocation().getBlockY();
        int z = event.getBlock().getLocation().getBlockZ();
        String blockname;
        blockname = event.getBlock().getType().toString();
        blockname = blockname.replaceAll("_", " ");
        String staff = "false";
        String serverName = main.getConfig().getString("Server-Name");
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        if (player.hasPermission("logger.exempt")){ return; }

        if (!event.isCancelled() && (main.getConfig().getBoolean("Log-to-Files")) && (main.getConfig().getBoolean("Log.Block-Place"))) {

            if (main.getConfig().getBoolean("Staff.Enabled") && player.hasPermission("logger.staff")){

                Discord.staffChat(player, "\uD83E\uDDF1️ **|** \uD83D\uDC6E\u200D♂️ [" + worldname + "] Has placed **" + blockname + "** at X = " + x + " Y = " + y + " Z = " + z, false, Color.green);

                try {

                    BufferedWriter out = new BufferedWriter(new FileWriter(FileHandler.getStaffFile(), true));
                    out.write("[" + dateFormat.format(date) + "] " + "[" + worldname + "] The Staff <" + playername + "> has placed " + blockname + " at X= " + x + " Y= " + y + " Z= " + z + "\n");
                    out.close();

                    if (main.getConfig().getBoolean("MySQL.Enable") && (main.getConfig().getBoolean("Log.Block-Place")) && (main.SQL.isConnected())) {

                        staff = "true";

                        MySQLData.blockPlace(serverName, worldname, playername, blockname, x, y, z, staff);

                    }

                } catch (IOException e) {

                    System.out.println("An error occurred while logging into the appropriate file.");
                    e.printStackTrace();

                }

                return;

            }

            Discord.blockPlace(player, "\uD83E\uDDF1️ [" + worldname + "] Has placed **" + blockname + "** at X = " + x + " Y = " + y + " Z = " + z, false, Color.green);

            try {

                BufferedWriter out = new BufferedWriter(new FileWriter(FileHandler.getBlockPlaceLogFile(), true));
                out.write("[" + dateFormat.format(date) + "] " + "[" + worldname + "] The Player <" + playername + "> has placed " + blockname + " at X= " + x + " Y= " + y + " Z= " + z + "\n");
                out.close();

            } catch (IOException e) {

                System.out.println("An error occurred while logging into the appropriate file.");
                e.printStackTrace();

            }
        }

        if (main.getConfig().getBoolean("MySQL.Enable") && (main.getConfig().getBoolean("Log.Block-Place")) && (main.SQL.isConnected())){

            try {

                MySQLData.blockPlace(serverName, worldname, playername, blockname, x, y, z, staff);

            }catch (Exception e){

                e.printStackTrace();

            }
        }
    }
}

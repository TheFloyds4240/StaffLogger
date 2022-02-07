package com.carpour.loggerbungeecord.Events.OnCommands;

import com.carpour.loggerbungeecord.Database.MySQL.MySQLData;
import com.carpour.loggerbungeecord.Database.SQLite.SQLiteData;
import com.carpour.loggerbungeecord.Discord.Discord;
import com.carpour.loggerbungeecord.Main;
import com.carpour.loggerbungeecord.Utils.FileHandler;
import com.carpour.loggerbungeecord.Utils.Messages;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class OnCommand implements Listener {

    private final Main main = Main.getInstance();

    @EventHandler
    public void onCmd(ChatEvent event) {

        if (event.isCommand()) {

            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String playerName = player.getName();
            String server = player.getServer().getInfo().getName();
            String command = event.getMessage();
            List<String> commandParts = Arrays.asList(command.split("\\s+"));
            String serverName = main.getConfig().getString("Server-Name");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            if (main.getConfig().getBoolean("Player-Commands.Whitelist-Commands")
                    && main.getConfig().getBoolean("Player-Commands.Blacklist-Commands")) return;

            if (player.hasPermission("loggerproxy.exempt")) return;

            if (main.getConfig().getBoolean("Player-Commands.Blacklist-Commands")) {

                for (String m : main.getConfig().getStringList("Player-Commands.Commands-to-Block")) {

                    if (commandParts.get(0).equalsIgnoreCase(m)) return;

                }
            }

            //Whitelist Commands
            if (main.getConfig().getBoolean("Player-Commands.Whitelist-Commands")){

                OnCommandWhitelist whitelist = new OnCommandWhitelist();
                whitelist.onWhitelistedCommand(event);

                return;
            }

            if (!event.isCancelled() && main.getConfig().getBoolean("Log-Player.Commands")) {

                //Log To Files Handling
                if (main.getConfig().getBoolean("Log-to-Files")) {

                    if (main.getConfig().getBoolean("Staff.Enabled") && player.hasPermission("loggerproxy.staff.log")) {

                        if (!Messages.getString("Discord.Player-Commands-Staff").isEmpty()) {

                            Discord.staffChat(player, Objects.requireNonNull(Messages.getString("Discord.Player-Commands-Staff")).replaceAll("%time%", dateTimeFormatter.format(ZonedDateTime.now())).replaceAll("%server%", server).replaceAll("%command%", command), false);

                        }

                        try {

                            BufferedWriter out = new BufferedWriter(new FileWriter(FileHandler.getStaffLogFile(), true));
                            out.write(Messages.getString("Files.Player-Commands-Staff").replaceAll("%time%", dateTimeFormatter.format(ZonedDateTime.now())).replaceAll("%server%", server).replaceAll("%player%", player.getName()).replaceAll("%command%", command) + "\n");
                            out.close();

                        } catch (IOException e) {

                            Main.getInstance().getLogger().warning("An error occurred while logging into the appropriate file.");
                            e.printStackTrace();

                        }

                        if (main.getConfig().getBoolean("MySQL.Enable") && main.mySQL.isConnected()) {


                            MySQLData.playerCommands(serverName, playerName, command, true);

                        }

                        if (main.getConfig().getBoolean("SQLite.Enable") && main.getSqLite().isConnected()) {

                            SQLiteData.insertPlayerCommands(serverName, playerName, command, true);

                        }

                        return;

                    }

                    try {

                        BufferedWriter out = new BufferedWriter(new FileWriter(FileHandler.getCommandLogFile(), true));
                        out.write(Messages.getString("Files.Player-Commands").replaceAll("%time%", dateTimeFormatter.format(ZonedDateTime.now())).replaceAll("%server%", server).replaceAll("%player%", player.getName()).replaceAll("%command%", command) + "\n");
                        out.close();

                    } catch (IOException e) {

                        Main.getInstance().getLogger().warning("An error occurred while logging into the appropriate file.");
                        e.printStackTrace();

                    }
                }

                //Discord Integration
                if (main.getConfig().getBoolean("Staff.Enabled") && player.hasPermission("loggerproxy.staff.log")) {

                    if (!Messages.getString("Discord.Player-Commands-Staff").isEmpty()) {

                        Discord.staffChat(player, Objects.requireNonNull(Messages.getString("Discord.Player-Commands-Staff")).replaceAll("%time%", dateTimeFormatter.format(ZonedDateTime.now())).replaceAll("%server%", server).replaceAll("%command%", command), false);

                    }

                } else {

                    if (!Messages.getString("Discord.Player-Commands").isEmpty()) {

                        Discord.playerCommands(player, Objects.requireNonNull(Messages.getString("Discord.Player-Commands")).replaceAll("%time%", dateTimeFormatter.format(ZonedDateTime.now())).replaceAll("%server%", server).replaceAll("%command%", command), false);

                    }
                }


                //MySQL Handling
                if (main.getConfig().getBoolean("MySQL.Enable") && main.mySQL.isConnected()) {

                    try {

                        MySQLData.playerCommands(serverName, playerName, command, player.hasPermission("loggerproxy.staff.log"));

                    } catch (Exception e) {

                        e.printStackTrace();

                    }
                }

                //SQLite Handling
                if (main.getConfig().getBoolean("SQLite.Enable") && main.getSqLite().isConnected()) {

                    try {

                        SQLiteData.insertPlayerCommands(serverName, playerName, command, player.hasPermission("loggerproxy.staff.log"));

                    } catch (Exception exception) {

                        exception.printStackTrace();

                    }
                }
            }
        }
    }
}
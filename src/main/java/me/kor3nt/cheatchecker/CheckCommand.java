package me.kor3nt.cheatchecker;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CheckCommand  implements CommandExecutor, Listener {

    private final FileConfiguration config;
    private Player hacker;
    Location playerLocation = null;
    YamlConfiguration file;
    File folders;
    public CheckCommand(FileConfiguration config, YamlConfiguration file, File folders) {
        this.config = config;
        this.folders=folders;
        this.file = file;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(format(config.getString("messages.if-console")));
            return true;
        }

        Player admin = (Player) sender;


        if(args.length == 0){
            admin.sendMessage(ChatColor.RED + "Cheat Checker");
            admin.sendMessage(ChatColor.YELLOW + "/checker check <nick> " + ChatColor.WHITE + "- you can check the player");
            admin.sendMessage(ChatColor.YELLOW + "/checker clear " + ChatColor.WHITE + " you acquitted the player");
            admin.sendMessage(ChatColor.YELLOW + "/checker admit " + ChatColor.WHITE + " the player pleaded guilty");
            admin.sendMessage(ChatColor.YELLOW + "/checker detection " + ChatColor.WHITE + " cheats found on the player");
        }
        else if(args[0].equalsIgnoreCase("check")) {
            if(args.length <= 1){
                admin.sendMessage(format(config.getString("messages.error-checking-player")));
                return true;
            }

            Location checkingRoom = file.getLocation("checkingRoom");

            if (checkingRoom == null) {
                admin.sendMessage(format(config.getString("messages.error-location")));
                return true;
            }

                if(hacker != null){
                    admin.sendMessage(format(config.getString("messages.already-player-checking")));
                    return true;
                }

                Player player = Bukkit.getPlayer(args[1]);
                if (player != null && player.isOnline()) {
                    playerLocation = player.getLocation();
                    player.teleport(checkingRoom);
                    admin.teleport(checkingRoom);

                    hacker = player;
                    player.sendTitle(format(config.getString("messages.cheater-title-up")), format(config.getString("messages.cheater-title-down")), 1, 30, 1);
                    player.sendMessage(format(config.getString("messages.chat-send-to-cheater")));
                    admin.sendMessage(format(config.getString("messages.send-to-admin")));
                    return true;
                }
                else{
                    admin.sendMessage(format(config.getString("messages.player-is-not-online")));
                    return true;
                }

        }
        else if (args[0].equalsIgnoreCase("clear")) {
            if(hacker == null) {
                admin.sendMessage(format(config.getString("messages.must-check")));
                return true;
            }

            hacker.teleport(playerLocation);
            hacker.sendMessage(format(config.getString("messages.player-is-not-guilty")));
            admin.sendMessage(format(config.getString("messages.admin-player-is-not-guilty")));
            hacker = null;
            playerLocation = null;
            return true;
        }

        else if (args[0].equalsIgnoreCase("admit")) {
            if(hacker == null) {
                admin.sendMessage(format(config.getString("messages.must-check")));
                return true;
            }
            long HOUR = 3600*1000;
            long time = config.getLong("ban-time-hours.admit") * HOUR;
            Date oldDate = new Date();
            Date newDate = new Date(oldDate.getTime() + time);
            hacker.getPlayer().getServer().getBanList(BanList.Type.NAME).addBan(hacker.getName(), config.getString("messages.player-detection"), newDate, admin.getName());
            hacker.kickPlayer(admin.getName());
            admin.sendMessage(format(config.getString("messages.admin-player-admit")));
            hacker = null;
            playerLocation = null;
            return true;
        }

        else if (args[0].equalsIgnoreCase("detection")) {
            if(hacker == null) {
                admin.sendMessage(format(config.getString("messages.must-check")));
                return true;
            }

            long HOUR = 3600*1000;
            long time = config.getLong("ban-time-hours.detection") * HOUR;
            Date oldDate = new Date();
            Date newDate = new Date(oldDate.getTime() + time);
            hacker.getPlayer().getServer().getBanList(BanList.Type.NAME).addBan(hacker.getName(), config.getString("messages.player-detection"), newDate, admin.getName());
            hacker.kickPlayer(admin.getName());
            admin.sendMessage(format(config.getString("messages.admin-player-detection")));
            hacker = null;
            playerLocation = null;
            return true;
        }
        else if(args[0].equalsIgnoreCase("set")){

            file.set("checkingRoom", admin.getLocation());
            admin.sendMessage(format(config.getString("messages.set-checking-room")));

            try {
                file.save(folders);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        else{
            admin.sendMessage(ChatColor.RED + "Cheat Checker");
            admin.sendMessage(ChatColor.YELLOW + "/checker check <nick> " + ChatColor.WHITE + "- you can check the player");
            admin.sendMessage(ChatColor.YELLOW + "/checker clear " + ChatColor.WHITE + " you acquitted the player");
            admin.sendMessage(ChatColor.YELLOW + "/checker admit " + ChatColor.WHITE + " the player pleaded guilty");
            admin.sendMessage(ChatColor.YELLOW + "/checker detection " + ChatColor.WHITE + " cheats found on the player");
            return true;
        }
        return true;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().equals(hacker)) {
            event.setCancelled(true);
            event.getPlayer().sendTitle(format(config.getString("messages.cheater-title-up")), format(config.getString("messages.cheater-title-down")), 1, 15, 1);
            event.getPlayer().sendMessage(format(config.getString("messages.chat-send-to-cheater")));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (event.getPlayer().equals(hacker)) {
            Date newDate = null;

            if(config.getLong("ban-time-hours.quit") == 0){
                newDate = null;
            }
            else{
                long HOUR = 3600*1000;
                long time = config.getLong("ban-time-hours.quit") * HOUR;
                Date oldDate = new Date();
                newDate = new Date(oldDate.getTime() + time);
            }

            event.getPlayer().getServer().getBanList(BanList.Type.NAME).addBan(hacker.getName(), config.getString("player-quit") , newDate, "Konsola");
            playerLocation = null;
            hacker = null;
        }
    }

    private String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}

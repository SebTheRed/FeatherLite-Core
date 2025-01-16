package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.teleportation.HomeManager;
import com.featherlite.pluginBin.essentials.teleportation.TeleportationManager;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeCommands implements TabCompleter {
    private final HomeManager homeManager;
    private final TeleportationManager teleportationManager;

    public HomeCommands(HomeManager homeManager, TeleportationManager teleportationManager) {
        this.homeManager = homeManager;
        this.teleportationManager = teleportationManager;
    }

    public boolean handleHomeCommands(CommandSender sender, Command command, String label, String[] args, boolean isPlayer) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use home commands.");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "sethome":
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /sethome <name>");
                    return true;
                }
            
                // Check if the player has permission for unlimited homes
                if (player.hasPermission("core.homes.unlimited") || sender.isOp()) {
                    homeManager.setHome(player, args[0], player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Home '" + args[0] + "' has been set!");
                    return true;
                }
            
                // Determine the max homes allowed by checking permissions
                int maxHomes = getMaxHomesAllowed(player);
            
                // Check if the player has exceeded their max homes limit
                if (homeManager.listHomes(player).size() >= maxHomes) {
                    player.sendMessage(ChatColor.RED + "You have reached the maximum number of homes allowed (" + maxHomes + ").");
                    return true;
                }
            
                // Set the home
                homeManager.setHome(player, args[0], player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Home '" + args[0] + "' has been set!");
                return true;
            
            case "home":
                if (isPlayer && !(sender.hasPermission("core.home") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /home <name>");
                    return true;
                }
                Location home = homeManager.getHome(player, args[0]);
                if (home == null) {
                    player.sendMessage(ChatColor.RED + "Home '" + args[0] + "' does not exist!");
                    return true;
                }
                teleportationManager.saveLastLocation(player);
                player.teleport(home);
                player.sendMessage(ChatColor.GREEN + "Teleported to home '" + args[0] + "'.");
                return true;

            case "delhome":
                if (isPlayer && !(sender.hasPermission("core.home") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /delhome <name>");
                    return true;
                }
                if (homeManager.deleteHome(player, args[0])) {
                    player.sendMessage(ChatColor.GREEN + "Home '" + args[0] + "' has been deleted!");
                } else {
                    player.sendMessage(ChatColor.RED + "Home '" + args[0] + "' does not exist!");
                }
                return true;

            case "homes":
                if (isPlayer && !(sender.hasPermission("core.home") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                List<String> homes = homeManager.listHomes(player);
                if (homes.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "You have no homes set.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Your homes: " + String.join(", ", homes));
                }
                return true;

            default:
                player.sendMessage(ChatColor.RED + "Unknown command.");
                return true;
        }
    }



    private int getMaxHomesAllowed(Player player) {
        // List of permissions from highest to lowest
        int[] homeLimits = {50, 40, 30, 20, 19, 18, 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
        for (int limit : homeLimits) {
            if (player.hasPermission("core.homes." + limit)) {
                return limit;
            }
        }
        // Default to 0 homes if no permissions are set
        return 0;
    }



    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
    
        Player player = (Player) sender;
    
        if ((alias.equalsIgnoreCase("home") || alias.equalsIgnoreCase("delhome"))) {
            // Suggest existing home names for "home" and "delhome"
            return homeManager.listHomes(player);
        }
    
        return Collections.emptyList();
    }
}

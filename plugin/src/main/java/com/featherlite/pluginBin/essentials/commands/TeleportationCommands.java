package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.teleportation.TeleportationManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TeleportationCommands implements TabCompleter {
    private final TeleportationManager teleportationManager;
    private final JavaPlugin plugin;

    public TeleportationCommands(TeleportationManager teleportationManager, JavaPlugin plugin) {
        this.teleportationManager = teleportationManager;
        this.plugin = plugin;
    }

    public boolean handleTeleportCommands(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use teleport commands.");
            return true;
        }

        Player player = (Player) sender;

        // if (args.length == 0) {
        //     player.sendMessage(ChatColor.RED + "Usage: /<command> [args]");
        //     return true;
        // }

        switch (label.toLowerCase()) {
            case "tppos":
                return handleTppos(player, args);
            case "tp":
                return handleTp(player, args);
            case "tphere":
                return handleTphere(player, args);
            case "tpall":
                return handleTpAll(player);
            case "tpa":
                return handleTpa(player, args);
            case "tpahere":
                return handleTpahere(player, args);
            case "tpaccept":
                return teleportationManager.acceptTeleport(player);
            case "tpadeny":
                return teleportationManager.denyTeleport(player);
            case "tpacancel":
                return teleportationManager.cancelTeleport(player);            
            case "back":
                return teleportationManager.teleportBack(player);
            case "tpr":
            case "rtp":
                return handleTpr(player);
            case "spawn":
                return teleportationManager.teleportToSpawn(player);
            case "setspawn":
                if (!player.hasPermission("feathercore.setspawn")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set the server spawn.");
                    return true;
                }
                Location location = player.getLocation();
                return teleportationManager.setServerSpawn(player, location);
            default:
                player.sendMessage(ChatColor.RED + "Unknown command.");
                return true;
        }
    }

    private boolean handleTppos(Player player, String[] args) {
        if (args.length < 3) {
            String worldName = args[3];
            if (Bukkit.getWorld(worldName) == null) {
                player.sendMessage(ChatColor.RED + "World '" + worldName + "' does not exist.");
                return true;
            }
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            Location location = args.length > 3
                    ? new Location(Bukkit.getWorld(args[3]), x, y, z)
                    : new Location(player.getWorld(), x, y, z);
            teleportationManager.saveLastLocation(player);
            player.teleport(location);
            player.sendMessage(ChatColor.GREEN + "Teleported to coordinates.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid coordinates.");
        }
        return true;
    }

    private boolean handleTp(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tp <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player " + args[0] + " not found.");
            return true;
        }

        teleportationManager.saveLastLocation(player);
        player.teleport(target.getLocation());
        player.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName());
        return true;
    }

    private boolean handleTpa(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
            return true;
        }

        Player tpaTarget = Bukkit.getPlayer(args[0]);
        if (tpaTarget == null || !tpaTarget.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        teleportationManager.requestTeleport(player, tpaTarget);
        return true;
    }

    private boolean handleTpahere(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tpahere <player>");
            return true;
        }
    
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
    
        teleportationManager.requestTeleportHere(player, target);
        return true;
    }

    private boolean handleTphere(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /tphere <player>");
            return true;
        }
    
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
    
        teleportationManager.saveLastLocation(target);
        target.teleport(player.getLocation());
        target.sendMessage(ChatColor.YELLOW + "You have been teleported to " + player.getName());
        player.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to you.");
        return true;
    }

    private boolean handleTpAll(Player player) {
        if (!player.hasPermission("feathercore.tpall")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
    
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(player)) {
                teleportationManager.saveLastLocation(target);
                target.teleport(player.getLocation());
                target.sendMessage(ChatColor.YELLOW + "You have been teleported to " + player.getName());
            }
        }
    
        player.sendMessage(ChatColor.GREEN + "All players have been teleported to you.");
        return true;
    }

    private boolean handleTpr(Player player) {
        int minRadius = plugin.getConfig().getInt("tpr-min-radius", 100);
        int maxRadius = plugin.getConfig().getInt("tpr-max-radius", 2000);
        player.sendMessage(ChatColor.YELLOW + "Random teleportation initiating...");

        if (teleportationManager.teleportRandomly(player, minRadius, maxRadius)) {
            player.sendMessage(ChatColor.GREEN + "Random teleportation successful!");
        } else {
            player.sendMessage(ChatColor.RED + "Random teleportation failed. Try again.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        List<String> suggestions = new ArrayList<>();
        switch (alias.toLowerCase()) {
            case "tp":
            case "tpa":
            case "tphere":
            case "tpahere":
                if (args.length == 1) {
                    // Suggest online players' names
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(player.getName());
                    }
                }
                break;
            case "tppos":
                if (args.length <= 4) {
                    suggestions.add("<coordinate>");
                }
                if (args.length == 4) {
                    Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
                }
                break;
            case "tpaccept":
            case "tpdeny":
            case "back":
            case "tpr":
            case "rtp":
                // No arguments for these commands
                break;
        }

        return suggestions;
    }
}

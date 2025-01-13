package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.teleportation.TeleportationManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;

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

    public boolean handleTeleportCommands(CommandSender sender, Command command, String label, String[] args, boolean isPlayer) {
        // Ensure the sender is a player for commands that require it
        Player executor = isPlayer ? (Player) sender : null;

        if (!isPlayer && !label.equalsIgnoreCase("tppos") && !label.equalsIgnoreCase("tp")) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by players.");
            return true;
        }

        switch (label.toLowerCase()) {
            case "tppos":
                return handleTppos(executor, args);

            case "tp":
                return handleTp(executor, args);

            case "tphere":
                return handleTphere(executor, args);

            case "tpall":
                return handleTpAll(executor);

            case "tpa":
                return handleTpa(executor, args);

            case "tpahere":
                return handleTpahere(executor, args);

            case "tpaccept":
                return teleportationManager.acceptTeleport(executor);

            case "tpadeny":
                return teleportationManager.denyTeleport(executor);

            case "tpacancel":
                return teleportationManager.cancelTeleport(executor);

            case "back":
                return teleportationManager.teleportBack(executor);

            case "tpr":
            case "rtp":
                return handleTpr(executor);

            case "spawn":
                return teleportationManager.teleportToSpawn(executor);

            case "setspawn":
                return handleSetSpawn(executor);

            default:
                sender.sendMessage(ChatColor.RED + "Unknown command.");
                return true;
        }
    }

    private boolean handleTppos(Player executor, String[] args) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        if (args.length < 3) {
            executor.sendMessage(ChatColor.RED + "Usage: /tppos <x> <y> <z> [world]");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);
            Location location = args.length > 3
                    ? new Location(Bukkit.getWorld(args[3]), x, y, z)
                    : new Location(executor.getWorld(), x, y, z);

            if (location.getWorld() == null) {
                executor.sendMessage(ChatColor.RED + "Invalid world specified.");
                return true;
            }

            teleportationManager.saveLastLocation(executor);
            executor.teleport(location);
            executor.sendMessage(ChatColor.GREEN + "Teleported to coordinates.");
        } catch (NumberFormatException e) {
            executor.sendMessage(ChatColor.RED + "Invalid coordinates.");
        }

        return true;
    }

    private boolean handleTp(Player executor, String[] args) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        if (args.length < 1) {
            executor.sendMessage(ChatColor.RED + "Usage: /tp <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            executor.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        teleportationManager.saveLastLocation(executor);
        executor.teleport(target.getLocation());
        executor.sendMessage(ChatColor.GREEN + "Teleported to " + target.getName() + ".");
        return true;
    }

    private boolean handleTphere(Player executor, String[] args) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        if (args.length < 1) {
            executor.sendMessage(ChatColor.RED + "Usage: /tphere <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            executor.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        teleportationManager.saveLastLocation(target);
        target.teleport(executor.getLocation());
        target.sendMessage(ChatColor.YELLOW + "You have been teleported to " + executor.getName() + ".");
        executor.sendMessage(ChatColor.GREEN + "Teleported " + target.getName() + " to you.");
        return true;
    }

    private boolean handleTpAll(Player executor) {
        if (executor == null || !executor.hasPermission("feathercore.tpall")) {
            executor.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!target.equals(executor)) {
                teleportationManager.saveLastLocation(target);
                target.teleport(executor.getLocation());
                target.sendMessage(ChatColor.YELLOW + "You have been teleported to " + executor.getName() + ".");
            }
        }

        executor.sendMessage(ChatColor.GREEN + "All players have been teleported to you.");
        return true;
    }

    private boolean handleTpa(Player executor, String[] args) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        if (args.length < 1) {
            executor.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            executor.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        teleportationManager.requestTeleport(executor, target);
        return true;
    }

    private boolean handleTpahere(Player executor, String[] args) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        if (args.length < 1) {
            executor.sendMessage(ChatColor.RED + "Usage: /tpahere <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            executor.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        teleportationManager.requestTeleportHere(executor, target);
        return true;
    }

    private boolean handleTpr(Player executor) {
        if (executor == null) {
            Bukkit.getLogger().warning("This command must be run by a player.");
            return true;
        }

        int minRadius = plugin.getConfig().getInt("tpr-min-radius", 100);
        int maxRadius = plugin.getConfig().getInt("tpr-max-radius", 2000);

        executor.sendMessage(ChatColor.YELLOW + "Random teleportation initiating...");
        if (teleportationManager.teleportRandomly(executor, minRadius, maxRadius)) {
            executor.sendMessage(ChatColor.GREEN + "Random teleportation successful!");
        } else {
            executor.sendMessage(ChatColor.RED + "Random teleportation failed. Try again.");
        }

        return true;
    }

    private boolean handleSetSpawn(Player executor) {
        if (executor == null || !executor.hasPermission("feathercore.setspawn")) {
            executor.sendMessage(ChatColor.RED + "You do not have permission to set the spawn.");
            return true;
        }

        Location location = executor.getLocation();
        teleportationManager.setServerSpawn(executor, location);
        executor.sendMessage(ChatColor.GREEN + "Server spawn point set.");
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
                    Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));
                }
                break;
            case "tppos":
                if (args.length <= 3) suggestions.add("<coordinate>");
                if (args.length == 4) Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
                break;
        }

        return suggestions;
    }
}


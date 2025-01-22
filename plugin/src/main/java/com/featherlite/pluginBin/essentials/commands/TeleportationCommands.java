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
                if (isPlayer && !(sender.hasPermission("core.teleport.tppos") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTppos(executor, args);

            case "tp":
                if (isPlayer && !(sender.hasPermission("core.teleport.tp") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTp(executor, args);

            case "tphere":
                if (isPlayer && !(sender.hasPermission("core.teleport.tphere") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTphere(executor, args);

            case "tpall":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpall") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTpAll(executor);

            case "tpa":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpa") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTpa(executor, args);

            case "tpahere":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpa") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTpahere(executor, args);

            case "tpaccept":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpa") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return teleportationManager.acceptTeleport(executor);

            case "tpdeny":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpa") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return teleportationManager.denyTeleport(executor);

            case "tpcancel":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpa") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return teleportationManager.cancelTeleport(executor);

            case "back":
                if (isPlayer && !(sender.hasPermission("core.teleport.back") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return teleportationManager.teleportBack(executor);

            case "tpr":
            case "rtp":
                if (isPlayer && !(sender.hasPermission("core.teleport.tpr") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return handleTpr(executor);

            case "spawn":
                if (isPlayer && !(sender.hasPermission("core.teleport.spawn") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return teleportationManager.teleportToSpawn(executor);

            case "setspawn":
                if (isPlayer && !(sender.hasPermission("core.teleport.setspawn") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
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

        Location location = executor.getLocation();
        teleportationManager.setServerSpawn(executor, location);
        executor.sendMessage(ChatColor.GREEN + "Server spawn point set.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
    
        Player player = (Player) sender;
        List<String> suggestions = new ArrayList<>();
    
        switch (alias.toLowerCase()) {
            case "tp":
            case "tpa":
            case "tphere":
            case "tpahere":
                if ((alias.equalsIgnoreCase("tp") && player.hasPermission("core.teleport.tp")) ||
                    (alias.equalsIgnoreCase("tpa") && player.hasPermission("core.teleport.tpa")) ||
                    (alias.equalsIgnoreCase("tphere") && player.hasPermission("core.teleport.tphere")) ||
                    (alias.equalsIgnoreCase("tpahere") && player.hasPermission("core.teleport.tpa")) ||
                    player.isOp()) {
                    if (args.length == 1) {
                        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> suggestions.add(onlinePlayer.getName()));
                    }
                }
                break;
    
            case "tppos":
                if (player.hasPermission("core.teleport.tppos") || player.isOp()) {
                    if (args.length <= 3) suggestions.add("<coordinate>");
                    if (args.length == 4) Bukkit.getWorlds().forEach(world -> suggestions.add(world.getName()));
                }
                break;
    
            case "tpall":
                if (player.hasPermission("core.teleport.tpall") || player.isOp()) {
                    // No specific arguments for /tpall, no suggestions required.
                }
                break;
    
            case "back":
                if (player.hasPermission("core.teleport.back") || player.isOp()) {
                    // No specific arguments for /back, no suggestions required.
                }
                break;
    
            case "tpr":
            case "rtp":
                if (player.hasPermission("core.teleport.tpr") || player.isOp()) {
                    // No specific arguments for /tpr or /rtp, no suggestions required.
                }
                break;
    
            case "spawn":
                if (player.hasPermission("core.teleport.spawn") || player.isOp()) {
                    // No specific arguments for /spawn, no suggestions required.
                }
                break;
    
            case "setspawn":
                if (player.hasPermission("core.teleport.setspawn") || player.isOp()) {
                    // No specific arguments for /setspawn, no suggestions required.
                }
                break;
    
            case "tpaccept":
            case "tpadeny":
            case "tpacancel":
                if (player.hasPermission("core.teleport.tpa") || player.isOp()) {
                    // No specific arguments for /tpaccept, /tpadeny, or /tpacancel, no suggestions required.
                }
                break;
        }
    
        return suggestions;
    }
    
}


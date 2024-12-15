package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.worlds.WorldManager;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class WorldCommands implements TabCompleter {
    private final WorldManager worldManager;
    private final Map<Player, String> deleteConfirmationMap = new HashMap<>();

    public WorldCommands(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    // Command handler for /world command
    public boolean handleWorldCommands(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /world <create|copy|tp|delete|load|unload|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (!player.hasPermission("feathercore.world.create")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to create worlds.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world create <worldName> <environment> [type]");
                    return true;
                }
                String worldName = args[1];
                Environment environment;
                try {
                    environment = Environment.valueOf(args[2].toUpperCase());
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + "Invalid environment type.");
                    return true;
                }
                WorldType type = args.length > 3 ? WorldType.valueOf(args[3].toUpperCase()) : WorldType.NORMAL;
                worldManager.createNewWorld(worldName, environment, type);
                player.sendMessage(ChatColor.GREEN + "World created: " + worldName);
                break;

            case "copy":
                if (!player.hasPermission("feathercore.world.copy")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to copy worlds.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world copy <baseWorldName> <newWorldName>");
                    return true;
                }
                String baseWorldName = args[1];
                String newWorldName = args[2];
                if (worldManager.createInstanceWorld(baseWorldName, newWorldName) != null) {
                    player.sendMessage(ChatColor.GREEN + "Copied world " + baseWorldName + " to " + newWorldName);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to copy world: " + baseWorldName);
                }
                break;

            case "list":
                if (!player.hasPermission("feathercore.world.list")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to list worlds.");
                    return true;
                }
                List<String> loadedWorlds = worldManager.getLoadedWorlds();
                if (loadedWorlds.isEmpty()) {
                    player.sendMessage(ChatColor.YELLOW + "No worlds are currently loaded.");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Loaded Worlds:");
                    for (String loadedWorldName : loadedWorlds) {
                        player.sendMessage(ChatColor.GRAY + "- " + loadedWorldName);
                    }
                }
                break;

            case "tp":
                if (!player.hasPermission("feathercore.world.tp")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to teleport to worlds.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world tp <worldName>");
                    return true;
                }
                String targetWorldName = args[1];
                World targetWorld = Bukkit.getWorld(targetWorldName);

                if (targetWorld == null) {
                    player.sendMessage(ChatColor.RED + "World not found or not loaded.");
                    return true;
                }

                Location spawnLocation = targetWorld.getSpawnLocation();
                worldManager.teleportPlayer(player, spawnLocation);

                player.sendMessage(ChatColor.GREEN + "Teleported to the spawn of world: " + targetWorldName);
                break;

            case "import":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world import <worldName> [environment]");
                    return true;
                }
                String importWorldName = args[1];
                World.Environment importEnvironment;
            
                if (args.length >= 3) {
                    // Try to parse the environment if provided
                    try {
                        importEnvironment = Environment.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        player.sendMessage(ChatColor.RED + "Invalid environment type. Available types: NORMAL, NETHER, THE_END.");
                        return true;
                    }
                } else {
                    // Default to NORMAL if no environment is specified
                    importEnvironment = Environment.NORMAL;
                    player.sendMessage(ChatColor.YELLOW + "No environment specified. Defaulting to NORMAL.");
                }
            
                if (worldManager.importWorld(importWorldName, importEnvironment)) {
                    worldManager.persistWorld(importWorldName);
                    player.sendMessage(ChatColor.GREEN + "World imported: " + importWorldName + " with environment: " + importEnvironment.name());
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to import world: " + importWorldName);
                }
                break;

            case "load":
                if (!player.hasPermission("feathercore.world.load")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to load worlds.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world load <worldName>");
                    return true;
                }
                if (worldManager.loadWorld(args[1]) != null) {
                    player.sendMessage(ChatColor.GREEN + "World loaded: " + args[1]);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to load world: " + args[1]);
                }
                break;

            case "unload":
                if (!player.hasPermission("feathercore.world.unload")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to unload worlds.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world unload <worldName>");
                    return true;
                }
                worldManager.unloadWorld(args[1]);
                player.sendMessage(ChatColor.GREEN + "World unloaded: " + args[1]);
                break;
            case "setborder":
                if (!player.hasPermission("feathercore.world.setborder")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to set world borders.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world setborder <worldName> <radius>");
                    return true;
                }
                try {
                    String worldNameBorder = args[1];
                    double radius = Double.parseDouble(args[2]);
                    if (radius <= 0) {
                        player.sendMessage(ChatColor.RED + "Radius must be greater than 0.");
                        return true;
                    }
                    worldManager.setWorldBorder(worldNameBorder, radius);
                    player.sendMessage(ChatColor.GREEN + "World border set for " + worldNameBorder + " with radius: " + radius);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Radius must be a number.");
                }
                break;
            case "delete":
                if (!player.hasPermission("feathercore.world.delete")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to delete worlds.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Usage: /world delete <worldName>");
                    return true;
                }
                String worldToDelete = args[1];
                if (deleteConfirmationMap.containsKey(player) && deleteConfirmationMap.get(player).equals(worldToDelete)) {
                    worldManager.deleteWorld(worldToDelete);
                    player.sendMessage(ChatColor.GREEN + "World deleted: " + worldToDelete);
                    deleteConfirmationMap.remove(player);
                } else {
                    deleteConfirmationMap.put(player, worldToDelete);
                    player.sendMessage(ChatColor.RED + "Are you sure you want to delete " + worldToDelete + "? Type the command again to confirm.");
                }
                break;

            default:
                player.sendMessage(ChatColor.YELLOW + "Unknown command. Use /world <create|copy|tp|delete|load|unload|list>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;
    
        if (args.length == 1) {
            // Suggest main subcommands
            return Arrays.asList("create", "copy", "tp", "delete", "load", "unload", "import", "list", "setborder");
        }
    
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "tp":
                case "delete":
                case "unload":
                case "copy":
                case "setborder":
                    // Suggest loaded world names for these commands
                    List<String> worldNames = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worldNames.add(world.getName());
                    }
                    return worldNames;
                case "load":
                case "create":
                case "import":
                    return Arrays.asList("<world_name>");
            }
        }
    
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Suggest environments for creating worlds
            return Arrays.asList("NORMAL", "NETHER", "THE_END");
        }
    
        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            // Suggest world types for creating worlds
            return Arrays.asList("NORMAL", "FLAT", "AMPLIFIED");
        }
    
        if (args[0].equalsIgnoreCase("setborder")) {
            switch (args.length) {
                case 2:
                    // Suggest loaded world names
                    List<String> worldNames = new ArrayList<>();
                    for (World world : Bukkit.getWorlds()) {
                        worldNames.add(world.getName());
                    }
                    return worldNames;
                case 3:
                    return Collections.singletonList("<radius>");
            }
        }
    
        return Collections.emptyList();
    }
}

package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.GameInstance;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import com.featherlite.pluginBin.lobbies.TeamSelectorBook;
import com.featherlite.pluginBin.lobbies.GamesManager;
import com.featherlite.pluginBin.lobbies.GamesManager.GameData;

import net.md_5.bungee.api.ChatColor;

import com.featherlite.pluginBin.lobbies.GamesUI;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;

import java.util.*;
import java.util.stream.Collectors;

public class GameCommands implements TabCompleter {

    private final InstanceManager instanceManager;
    private final GamesManager gamesManager;
    private final GamesUI gamesUI;
    private final TeamSelectorBook teamSelectorBook;

    public GameCommands(InstanceManager instanceManager, GamesManager gamesManager, GamesUI gamesUI, TeamSelectorBook teamSelectorBook) {
        this.instanceManager = instanceManager;
        this.gamesManager = gamesManager;
        this.gamesUI = gamesUI;
        this.teamSelectorBook = teamSelectorBook;
    }

    public boolean handleGameCommands(CommandSender sender, String[] args, boolean isPlayer, JavaPlugin plugin) {
        if (!isPlayer) {
            if (args.length < 1 || !args[0].equalsIgnoreCase("create") && !args[0].equalsIgnoreCase("close")) {
                plugin.getLogger().warning("The /game command can only be used by players for most subcommands.");
                return true;
            }
        }
        // If no arguments are provided, default to opening the menu
        if (args.length == 0) {
            if (isPlayer) {
                if (!(sender.hasPermission("core.games.menu") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to open the games menu.");
                    return true;
                }
                gamesUI.openMainMenu((Player) sender);
            } else {
                plugin.getLogger().warning("The /game menu command can only be used by players.");
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "ui":
            case "menu":
                if (isPlayer) {
                    if (isPlayer && !(sender.hasPermission("core.games.menu") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                        return true;
                    }
                    gamesUI.openMainMenu((Player) sender);
                } else {
                    plugin.getLogger().warning("The /game menu command can only be used by players.");
                }
                return true;
    
            case "join":
                if (isPlayer) {
                    if (isPlayer && !(sender.hasPermission("core.games.play") || sender.isOp()) ) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                        return true;
                    }
                    handleJoinCommand((Player) sender, args);
                } else {
                    plugin.getLogger().warning("The /game join command can only be used by players.");
                }
                return true;
    
            case "create":
                if (isPlayer && !(sender.hasPermission("core.games.create") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                handleCreateCommand(sender, args);
                return true;
    
            case "delete":
                // if (isPlayer && !sender.hasPermission("core.games.create")) {
                //     sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                // }
                // if (isPlayer) {
                //     handleDeleteCommand((Player) sender, args);
                // } else {
                //     plugin.getLogger().warning("The /game delete command can only be used by players.");
                // }
                sender.sendMessage("This command doesn't do anything! Use close.");
                return true;
    
            case "leave":
                if (isPlayer) {
                    if (isPlayer && !(sender.hasPermission("core.games.play") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                        return true;
                    }
                    Player player = (Player) sender;
                    instanceManager.handlePlayerLeave(player);
                    player.sendMessage("You left the game.");
                } else {
                    plugin.getLogger().warning("The /game leave command can only be used by players.");
                }
                return true;
    
            case "close":
                if (isPlayer && !(sender.hasPermission("core.games.close") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                handleCloseCommand(sender, args, isPlayer);
                return true;
    
            default:
                if (isPlayer) {
                    ((Player) sender).sendMessage("Unknown command. Use /game <ui|menu|join|leave|create|delete|close>");
                } else {
                    plugin.getLogger().warning("Unknown /game subcommand.");
                }
                return true;
        }
    }

    private void handleJoinCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /game join <instanceID>");
            return;
        }

        try {
            UUID instanceId = UUID.fromString(args[1]);
            GameInstance instance = instanceManager.getInstance(instanceId);
            if (instance == null) {
                player.sendMessage("No game instance found with that ID.");
                return;
            }
            instanceManager.addPlayerToInstance(player, instance);
            player.sendMessage("You have joined the game instance!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid instance ID.");
        }
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("games.create")) {
                player.sendMessage("You do not have permission to create games.");
                return;
            }
    
            if (args.length < 4) {
                player.sendMessage("Usage: /game create <public|false> \"<game-name>\"  <world>");
                return;
            }
    
            // Extract quoted game name
            String gameName = extractQuotedArgument(args, 1);
            if (gameName == null) {
                player.sendMessage("Please provide the game name in quotes (e.g., \"Four Team Bedwars - Quads\").");
                return;
            }
    
            // Get world name
            String worldName = args[args.length - 1];
            if (worldName.isEmpty()) {
                player.sendMessage("You must specify a valid world name.");
                return;
            }
    
            // Start the game instance
            try {
                GameInstance newGame = gamesManager.startGameInstance(gameName, worldName, true, instanceManager, sender.getName(), true);
                if (newGame == null) {
                    player.sendMessage("Failed to create game instance. Please check the game name and world.");
                } else {
                    player.sendMessage("Game instance created successfully with ID: " + newGame.getInstanceId());
                }
            } catch (IllegalArgumentException e) {
                player.sendMessage("Invalid game name or world. Please check your input and try again.");
            } catch (Exception e) {
                player.sendMessage("An error occurred while creating the game instance. Check the server logs.");
                e.printStackTrace();
            }
        }
        
    }

    private void handleCloseCommand(CommandSender sender, String[] args, boolean isPlayer) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /game close <instanceID>");
            return;
        }

        try {
            UUID instanceId = UUID.fromString(args[1]);
            GameInstance closingInstance = instanceManager.getInstance(instanceId);
            if (sender.getName().equalsIgnoreCase(closingInstance.getCreatedBy()) || !isPlayer || sender.hasPermission("core.games.closeothers")) {
                String lobbyName = "spawn";
                instanceManager.closeInstance(instanceId);
                sender.sendMessage("Game instance: " + closingInstance.getInstanceId());
                sender.sendMessage("Closed successfully and players teleported to " + lobbyName + "!");
                return;
            } else {
                sender.sendMessage("Only admins / console can close a game instance!");
                return;
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid instance ID or lobby name.");
        }
    }

    private String extractQuotedArgument(String[] args, int startIndex) {
        StringBuilder quoted = new StringBuilder();
        boolean inQuotes = false;
    
        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
    
            if (arg.startsWith("\"") && !inQuotes) {
                // Starting a new quoted argument
                inQuotes = true;
                quoted.append(arg.substring(1)); // Remove starting quote
            } else if (arg.endsWith("\"") && inQuotes) {
                // Closing the quoted argument
                quoted.append(" ").append(arg, 0, arg.length() - 1); // Remove ending quote
                inQuotes = false;
                break; // End of quoted argument
            } else if (inQuotes) {
                // Inside a quoted argument
                quoted.append(" ").append(arg);
            } else {
                // If we're not inside quotes and not starting a quoted argument, return null
                return null;
            }
        }
    
        // Ensure quotes were balanced
        if (inQuotes) {
            return null; // Unbalanced quotes
        }
    
        return quoted.toString().trim();
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
    
        if (args.length == 1) {
            // Suggest subcommands based on permissions
            if (sender.hasPermission("core.games.menu") || sender.isOp()) {
                suggestions.add("menu");
                suggestions.add("ui");
            }
            if (sender.hasPermission("core.games.play") || sender.isOp()) {
                suggestions.add("join");
                suggestions.add("leave");
            }
            if (sender.hasPermission("core.games.create") || sender.isOp()) {
                suggestions.add("create");
            }
            if (sender.hasPermission("core.games.close") || sender.isOp()) {
                suggestions.add("close");
            }
        } else if (args.length == 2) {
            // For the "create" command, suggest available game names
            if (args[0].equalsIgnoreCase("create") && (sender.hasPermission("core.games.create") || sender.isOp())) {
                suggestions.addAll(
                    gamesManager.listRegisteredGames().stream()
                        .map(GameData::getGameName)
                        .map(name -> "\"" + name + "\"") // Ensure game names are quoted
                        .collect(Collectors.toList())
                );
            } else if (args[0].equalsIgnoreCase("close") && (sender.hasPermission("core.games.close") || sender.isOp())) {
                // Suggest only the UUIDs of instances created by the sender, unless they have the "closeothers" permission
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    suggestions.addAll(
                        instanceManager.getActiveInstances().values().stream()
                            .filter(instance -> instance.getCreatedBy().equalsIgnoreCase(player.getName()) || player.hasPermission("core.games.closeothers") || player.isOp())
                            .map(instance -> instance.getInstanceId().toString())
                            .collect(Collectors.toList())
                    );
                } else {
                    // For console, show all active instances
                    suggestions.addAll(
                        instanceManager.getActiveInstances().keySet().stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList())
                    );
                }
            } else if (args[0].equalsIgnoreCase("join") && (sender.hasPermission("core.games.play") || sender.isOp())) {
                // For "join" command, suggest active instance IDs
                suggestions.addAll(
                    instanceManager.getActiveInstances().keySet().stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList())
                );
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create") && (sender.hasPermission("core.games.create") || sender.isOp())) {
            // For the second argument of "create", suggest available worlds for the selected game
            String gameName = extractQuotedArgument(args, 1);
            if (gameName != null) {
                GameData gameData = gamesManager.getGameData(gameName);
                if (gameData != null) {
                    suggestions.addAll(gameData.getWorldOptions());
                }
            }
        }
    
        // Filter suggestions based on the current input
        return suggestions.stream()
            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
            .collect(Collectors.toList());
    }
    
    
    
}

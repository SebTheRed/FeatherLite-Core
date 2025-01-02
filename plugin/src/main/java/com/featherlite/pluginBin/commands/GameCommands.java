package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.GameInstance;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import com.featherlite.pluginBin.lobbies.TeamSelectorBook;
import com.featherlite.pluginBin.lobbies.GamesManager;
import com.featherlite.pluginBin.lobbies.GamesManager.GameData;
import com.featherlite.pluginBin.lobbies.GamesUI;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
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

    public boolean handleGameCommands(Player player, String[] args) {
        if (args.length < 1) {
            // Default to opening the Games UI
            gamesUI.openMainMenu(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "ui":
            case "menu":
                gamesUI.openMainMenu(player);
                return true;

            case "join":
                handleJoinCommand(player, args);
                return true;

            case "create":
                handleCreateCommand(player, args);
                return true;

            case "delete":
                handleDeleteCommand(player, args);
                return true;

            case "leave":
                instanceManager.handlePlayerLeave(player);
                player.sendMessage("You left the game.");
                return true;

            case "close":
                handleCloseCommand(player, args);
                return true;

            default:
                player.sendMessage("Unknown command. Use /game <ui|menu|join|leave|create|delete|close>");
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

    private void handleCreateCommand(Player player, String[] args) {
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
            GameInstance newGame = gamesManager.startGameInstance(gameName, worldName, true, instanceManager);
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

    private void handleDeleteCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /game delete <instanceID>");
            return;
        }

        try {
            UUID instanceId = UUID.fromString(args[1]);
            instanceManager.removeInstance(instanceId);
            player.sendMessage("Game instance deleted successfully!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid instance ID.");
        }
    }

    private void handleCloseCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("Usage: /game close <instanceID> <lobbyName>");
            return;
        }

        try {
            UUID instanceId = UUID.fromString(args[1]);
            String lobbyName = args[2];
            instanceManager.closeInstance(instanceId);
            player.sendMessage("Game instance closed successfully and players teleported to " + lobbyName + "!");
        } catch (IllegalArgumentException e) {
            player.sendMessage("Invalid instance ID or lobby name.");
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
            // Suggest subcommands
            suggestions.addAll(Arrays.asList("ui", "menu", "join", "leave", "create", "delete", "close"));
        } else if (args.length == 2) {
            // For the "create" command, suggest available game names
            if (args[0].equalsIgnoreCase("create")) {
                suggestions.addAll(
                    gamesManager.listRegisteredGames().stream()
                        .map(GameData::getGameName)
                        .map(name -> "\"" + name + "\"") // Ensure game names are quoted
                        .collect(Collectors.toList())
                );
            } else if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("close")) {
                // For other commands, suggest active instance IDs
                suggestions.addAll(
                    instanceManager.getActiveInstances().keySet().stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList())
                );
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
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

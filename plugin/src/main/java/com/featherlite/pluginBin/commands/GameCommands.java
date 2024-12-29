package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.GameInstance;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import com.featherlite.pluginBin.lobbies.GamesManager;
import com.featherlite.pluginBin.lobbies.GamesManager.GameData;
import com.featherlite.pluginBin.lobbies.GamesUI;

import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;

public class GameCommands implements TabCompleter {

    private final InstanceManager instanceManager;
    private final GamesManager gamesManager;
    private final GamesUI gamesUI;

    public GameCommands(InstanceManager instanceManager, GamesManager gamesManager, GamesUI gamesUI) {
        this.instanceManager = instanceManager;
        this.gamesManager = gamesManager;
        this.gamesUI = gamesUI;
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
                // Open the Games UI
                gamesUI.openMainMenu(player);
                return true;

            case "join":
                if (args.length < 2) {
                    player.sendMessage("Usage: /game join <instanceID>");
                    return true;
                }
                try {
                    UUID instanceId = UUID.fromString(args[1]);
                    GameInstance instance = instanceManager.getInstance(instanceId);
                    if (instance == null) {
                        player.sendMessage("No game instance found with that ID.");
                        return true;
                    }
                    instanceManager.addPlayerToInstance(player, instance, null);
                    player.sendMessage("You have joined the game instance!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid instance ID.");
                }
                break;

            case "create":
                if (!player.hasPermission("games.create")) {
                    player.sendMessage("You do not have permission to create games.");
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("Usage: /game create \"<game-name>\" <world>");
                    return true;
                }

                // Extract quoted game name
                String gameName = extractQuotedArgument(args, 1);
                if (gameName == null) {
                    player.sendMessage("Please provide the game name in quotes (e.g., \"Four Team Bedwars - Quads\").");
                    return true;
                }

                // Get the world name
                String worldName = args[args.length - 1];

                try {
                    GameInstance newGame = gamesManager.startGameInstance(gameName, worldName, instanceManager);
                    if (newGame == null) {
                        player.sendMessage("Failed to create game instance. Please check the game name and world.");
                    } else {
                        player.sendMessage("Game instance created successfully with ID: " + newGame.getInstanceId());
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid game name or world. Please check your input and try again.");
                } catch (Exception e) {
                    player.sendMessage("An error occurred while creating the game instance.");
                    e.printStackTrace(); // Log the error for debugging
                }
                break;

            case "delete":
                if (args.length < 2) {
                    player.sendMessage("Usage: /game delete <instanceID>");
                    return true;
                }
                try {
                    UUID instanceId = UUID.fromString(args[1]);
                    instanceManager.removeInstance(instanceId);
                    player.sendMessage("Game instance deleted successfully!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid instance ID.");
                }
                break;

            case "leave":
                instanceManager.handlePlayerLeave(player);
                player.sendMessage("You left the game.");
                break;

            case "close":
                if (args.length < 3) {
                    player.sendMessage("Usage: /game close <instanceID> <lobbyName>");
                    return true;
                }
                try {
                    UUID instanceId = UUID.fromString(args[1]);
                    String lobbyName = args[2];
                    instanceManager.closeInstance(instanceId, true, lobbyName);
                    player.sendMessage("Game instance closed successfully and players teleported to " + lobbyName + "!");
                } catch (IllegalArgumentException e) {
                    player.sendMessage("Invalid instance ID or lobby name.");
                }
                break;

            default:
                player.sendMessage("Unknown command. Use /game <ui|menu|join|leave|create|delete|close>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            // Suggest subcommands
            suggestions.addAll(Arrays.asList("ui", "menu", "join", "leave", "create", "delete", "close"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "create":
                    // Suggest registered game names for /game create
                    suggestions.addAll(gamesManager.listRegisteredGames().stream()
                            .map(GameData::getGameName)
                            .map(name -> "\"" + name + "\"") // Wrap names in quotes
                            .collect(Collectors.toList()));
                    break;
                case "join":
                case "delete":
                case "close":
                    // Suggest active instance IDs for /game join, delete, close
                    suggestions.addAll(instanceManager.getActiveInstances().keySet().stream()
                            .map(UUID::toString)
                            .collect(Collectors.toList()));
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Suggest available world names for /game create "<game-name>" <world>
            String gameName = extractQuotedArgument(args, 1);
            if (gameName != null) {
                GameData gameData = gamesManager.getGameData(gameName);
                if (gameData != null) {
                    suggestions.addAll(gameData.getWorldOptions()); // Suggest maps for the game
                }
            }
        }

        // Filter suggestions based on current input to make them dynamic
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Extracts a quoted argument from an array of arguments.
     * 
     * @param args The command arguments.
     * @param startIndex The index to start extracting from.
     * @return The quoted argument, or null if not found.
     */
    private String extractQuotedArgument(String[] args, int startIndex) {
        StringBuilder quoted = new StringBuilder();
        boolean inQuotes = false;

        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("\"")) {
                inQuotes = true;
                quoted.append(arg.substring(1)); // Remove starting quote
            } else if (arg.endsWith("\"")) {
                inQuotes = false;
                quoted.append(" ").append(arg, 0, arg.length() - 1); // Remove ending quote
                break;
            } else if (inQuotes) {
                quoted.append(" ").append(arg);
            } else {
                // Not in quotes, invalid argument
                return null;
            }
        }

        return inQuotes ? null : quoted.toString(); // Return null if quotes are unbalanced
    }
}

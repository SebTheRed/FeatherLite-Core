package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.GameInstance;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GameCommands {

    private final InstanceManager instanceManager;

    public GameCommands(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    public boolean handleGameCommands(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("Usage: /game <join|leave|create|delete>");
            return true;
        }

        switch (args[0].toLowerCase()) {
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
                    instanceManager.addPlayerToInstance(player, instance, null); // Passing null to let the manager handle team selection
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
                    player.sendMessage("Usage: /game create <category> <mode>");
                    return true;
                }
                String category = args[1];
                String mode = args[2];
                String worldName = args[3];
                GameInstance newGame = instanceManager.createInstance(category, mode, worldName);
                if (newGame == null) {
                    player.sendMessage("Failed to create game instance. Check the category and mode.");
                } else {
                    player.sendMessage("Game instance created successfully!");
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
                player.sendMessage("Unknown command. Use /game <join|leave|create|delete>");
        }
        return true;
    }
}

package com.featherlite.pluginBin.placeholders;

import com.featherlite.pluginBin.lobbies.GameInstance;
import com.featherlite.pluginBin.lobbies.GameInstance.GameState;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import org.bukkit.entity.Player;

import java.util.List;

public class PlaceholderLobbies {
    private static InstanceManager instanceManager;

    // Method to set the InstanceManager
    public static void setInstanceManager(InstanceManager manager) {
        instanceManager = manager;
    }

    // Placeholder: <game_state>
    public static String getGameState(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        return instance != null ? instance.getState().toString() : "No Game";
    }

    // Placeholder: <game_type>
    public static String getGameType(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        return instance != null ? instance.getGameType() : "No Game";
    }

    // Placeholder: <game_players>
    public static String getPlayerCount(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        if (instance != null) {
            int playerCount = instance.getTeams().values().stream().mapToInt(List::size).sum();
            return String.valueOf(playerCount);
        }
        return "0";
    }

    // Placeholder: <game_min_players>
    public static String getMinPlayers(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        return instance != null ? String.valueOf(instance.getMinPlayers()) : "0";
    }

    // Placeholder: <game_max_players>
    public static String getMaxPlayers(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        return instance != null ? String.valueOf(instance.getMaxPlayers()) : "0";
    }

    // Placeholder: <game_world>
    public static String getGameWorld(Player player) {
        GameInstance instance = getGameInstanceForPlayer(player);
        return instance != null ? instance.getWorldName() : "No World";
    }

    // Utility: Get the GameInstance for the player
    private static GameInstance getGameInstanceForPlayer(Player player) {
        if (instanceManager == null) return null;
        return instanceManager.getInstanceForPlayer(player);
    }
}

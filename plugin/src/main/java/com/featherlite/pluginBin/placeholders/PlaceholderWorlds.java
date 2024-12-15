package com.featherlite.pluginBin.placeholders;

import com.featherlite.pluginBin.worlds.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlaceholderWorlds {
    private static WorldManager worldManager;
    
    // Placeholder: <world_name>
    public static String getCurrentWorldName(Player player) {
        World world = player.getWorld();
        return world != null ? world.getName() : "Unknown World";
    }

    // Placeholder: <world_environment>
    public static String getCurrentWorldEnvironment(Player player) {
        World world = player.getWorld();
        return world != null ? world.getEnvironment().toString() : "Unknown Environment";
    }

    // Placeholder: <loaded_worlds>
    public static String getLoadedWorlds(Player player) {
        if (worldManager == null) return "No Worlds";
        return String.join(", ", worldManager.getLoadedWorlds());
    }

    // Placeholder: <world_players>
    public static String getPlayersInWorld(Player player) {
        World world = player.getWorld();
        if (world != null) {
            return String.valueOf(world.getPlayers().size());
        }
        return "0";
    }
}

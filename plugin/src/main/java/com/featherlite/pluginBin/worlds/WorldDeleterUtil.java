package com.featherlite.pluginBin.worlds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WorldDeleterUtil {
    private final JavaPlugin plugin;
    private final String fallbackWorldName; // The name of the fallback world where players will be teleported

    public WorldDeleterUtil(JavaPlugin plugin, String fallbackWorldName) {
        this.plugin = plugin;
        this.fallbackWorldName = fallbackWorldName; // Set the fallback world name
    }

    public void deleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            teleportPlayersToFallback(world); // Teleport players to the fallback world
            Bukkit.unloadWorld(world, false); // Unload the world after players are removed
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteWorldFolder(worldFolder);
            plugin.getLogger().info("Deleted world folder: " + worldName + " 🗑️");
        }
    }

    private void teleportPlayersToFallback(World world) {
        World fallbackWorld = Bukkit.getWorld(fallbackWorldName);
        if (fallbackWorld == null) {
            plugin.getLogger().warning("Fallback world " + fallbackWorldName + " not found!");
            return;
        }

        Location fallbackSpawnLocation = fallbackWorld.getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(fallbackSpawnLocation);
            player.sendMessage("You have been teleported to the fallback world as the world you were in is being deleted.");
        }
    }

    private void deleteWorldFolder(File folder) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                deleteWorldFolder(file);
            } else if (!file.delete()) {
                plugin.getLogger().warning("Failed to delete file: " + file.getAbsolutePath());
            }
        }
        if (!folder.delete()) {
            plugin.getLogger().warning("Failed to delete folder: " + folder.getAbsolutePath());
        }
    }
}

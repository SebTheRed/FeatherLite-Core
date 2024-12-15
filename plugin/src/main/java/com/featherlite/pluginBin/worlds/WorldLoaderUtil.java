package com.featherlite.pluginBin.worlds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WorldLoaderUtil {
    private final JavaPlugin plugin;

    public WorldLoaderUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Method to load an existing world from its folder
    public World loadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            plugin.getLogger().info("World already loaded: " + worldName);
            return world;
        }

        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists() || !new File(worldFolder, "level.dat").exists()) {
            plugin.getLogger().severe("World folder not found or invalid: " + worldName);
            return null;
        }

        plugin.getLogger().info("Importing and loading world: " + worldName);
        return Bukkit.createWorld(new WorldCreator(worldName));
    }

    // Unload a world by name
    public void unloadWorld(String worldName, boolean save) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            plugin.getLogger().info("Unloading world: " + worldName);
            Bukkit.unloadWorld(world, save);
        }
    }
}

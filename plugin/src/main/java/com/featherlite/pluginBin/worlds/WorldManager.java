package com.featherlite.pluginBin.worlds;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

import java.util.ArrayList;

public class WorldManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    private final WorldLoaderUtil loaderUtil;
    private final WorldCreatorUtil creatorUtil;
    private final WorldDeleterUtil deleterUtil;
    private final WorldCopyUtil copyUtil;
    private final WorldTeleporterUtil teleporterUtil;
    private final boolean isDebuggerOn;

    public WorldManager(JavaPlugin plugin, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.isDebuggerOn = isDebuggerOn;
        this.configFile = new File(plugin.getDataFolder(), "worlds-data.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.loaderUtil = new WorldLoaderUtil(plugin, isDebuggerOn);
        this.creatorUtil = new WorldCreatorUtil(plugin);
        this.deleterUtil = new WorldDeleterUtil(plugin, "world");
        this.copyUtil = new WorldCopyUtil(plugin, isDebuggerOn);
        this.teleporterUtil = new WorldTeleporterUtil(plugin);
    }

    // Method to create a new world from scratch
    public World createNewWorld(String worldName, Environment environment, WorldType type) {
        return creatorUtil.createNewWorld(worldName, environment, type);
    }

    // Method to copy an existing world for instance management
    public World createInstanceWorld(String baseWorldName, String instanceWorldName) {
        if (copyUtil.copyWorld(baseWorldName, instanceWorldName)) {
            return loaderUtil.loadWorld(instanceWorldName);
        }
        return null;
    }

    public boolean importWorld(String worldName, World.Environment environment) {
        World world = loaderUtil.loadWorld(worldName);
        if (world != null) {
            return true; // World was successfully loaded
        }
        return false; // World folder not found or invalid
    }

        // Load all worlds from the configuration file on startup
    public void loadPersistedWorlds() {
        List<String> worlds = config.getStringList("loadedWorlds");
        for (String worldName : worlds) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                if (isDebuggerOn) {plugin.getLogger().info("Loading persisted world: " + worldName);}
                // Use your WorldLoaderUtil or equivalent to load the world
                loaderUtil.loadWorld(worldName);
            }
        }
    }

    // Save the loaded world to configuration file and load the world into memory
    public void persistWorld(String worldName) {
        List<String> worlds = config.getStringList("loadedWorlds");

        // Add the world to the configuration if it doesn't exist
        if (!worlds.contains(worldName)) {
            worlds.add(worldName);
            config.set("loadedWorlds", worlds);
            saveConfig();
        }

        // Check if the world is already loaded in memory
        if (Bukkit.getWorld(worldName) == null) {
            // Load the world from disk
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                Bukkit.createWorld(new WorldCreator(worldName));
                Bukkit.getLogger().info("World '" + worldName + "' has been loaded and persisted.");
            } else {
                Bukkit.getLogger().warning("World '" + worldName + "' does not exist on disk. Cannot persist.");
            }
        }
    }


    // Remove a world from persisted list and unload it from the server
    public void removePersistedWorld(String worldName) {
        List<String> worlds = config.getStringList("loadedWorlds");

        // Remove the world from the configuration
        if (worlds.contains(worldName)) {
            worlds.remove(worldName);
            config.set("loadedWorlds", worlds);
            saveConfig();
        }

        // Unload the world from memory if it is currently loaded
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            boolean success = Bukkit.unloadWorld(world, true); // Save changes before unloading
            if (success) {
                Bukkit.getLogger().info("World '" + worldName + "' has been unloaded and removed from persistence.");
            } else {
                Bukkit.getLogger().warning("Failed to unload world '" + worldName + "'.");
            }
        } else {
            Bukkit.getLogger().info("World '" + worldName + "' is not currently loaded.");
        }
    }

    // Save configuration file
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save loaded worlds configuration!");
            e.printStackTrace();
        }
    }

    // Method to load an existing world by name
    public World loadWorld(String worldName) {
        return loaderUtil.loadWorld(worldName);
    }

    public void unloadWorld(String worldName) {
        loaderUtil.unloadWorld(worldName, false);
    }

    public List<String> getLoadedWorlds() {
        List<String> loadedWorlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            loadedWorlds.add(world.getName());
        }
        return loadedWorlds;
    }


    public void deleteWorld(String worldName) {
        unloadWorld(worldName); // Unload before deleting
        deleterUtil.deleteWorld(worldName);
    }

    public void teleportPlayer(Player player, Location location) {
        teleporterUtil.teleportPlayer(player, location);
    }



    // Set the square world border using a radius
    public void setWorldBorder(String worldName, double radius) {
        config.set("world-borders." + worldName + ".radius", radius);
        saveConfig();
        if (isDebuggerOn) {plugin.getLogger().info("Square world border set for " + worldName + " with radius: " + radius);}
    }

    // Get the square world border radius
    public Double getWorldBorder(String worldName) {
        if (!config.contains("world-borders." + worldName + ".radius")) {
            return null; // No border configured
        }
        return config.getDouble("world-borders." + worldName + ".radius");
    }

}

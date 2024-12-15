package com.featherlite.pluginBin.permissions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

public class PermissionsFileManager {
    private final JavaPlugin plugin;
    private final File pluginFolder;

    public PermissionsFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pluginFolder = plugin.getDataFolder();

        if (!pluginFolder.exists()) {
            pluginFolder.mkdirs();
        }

        // this.playersFolder = new File(pluginFolder, "player-permissions");
        // if (!playersFolder.exists()) {
        //     playersFolder.mkdirs();
        // }
    }

    public FileConfiguration loadConfig(String fileName) {
        File file = new File(pluginFolder, fileName);

        if (!file.exists()) {
            copyDefaultFile(fileName);
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveConfig(FileConfiguration config, String fileName) {
        File file = new File(pluginFolder, fileName);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // public FileConfiguration loadPlayerConfig(String playerName, String playerUUID) {
    //     // File name includes the player's username and UUID
    //     File playerFile = new File(playersFolder, playerName + "_" + playerUUID + ".yml");

    //     boolean isNewFile = false;

    //     // If the file does not exist, create it
    //     if (!playerFile.exists()) {
    //         try {
    //             playerFile.createNewFile(); // Create the file
    //             isNewFile = true;
    //             plugin.getLogger().info("Created new permissions file for player: " + playerName);
    //         } catch (IOException e) {
    //             plugin.getLogger().severe("Could not create permissions file for player: " + playerName);
    //             e.printStackTrace();
    //         }
    //     }

    //     // Load the configuration
    //     FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

    //     // If this is a new file, set up default groups
    //     if (isNewFile) {
    //         List<String> defaultGroups = plugin.getConfig().getStringList("first-join-permission-groups");
    //         playerConfig.set("groups", defaultGroups);
    //         plugin.getLogger().info("Assigned default groups to new player: " + playerName);

    //         try {
    //             playerConfig.save(playerFile); // Save the changes
    //         } catch (IOException e) {
    //             plugin.getLogger().severe("Failed to save permissions file for player: " + playerName);
    //             e.printStackTrace();
    //         }
    //     }

    //     return playerConfig;
    // }

    // public void savePlayerConfig(FileConfiguration config, String playerName, String uuid) {
    //     // File name includes the player's username and UUID
    //     String playerFileName = playerName + "_" + uuid + ".yml";
    //     File playerFile = new File(playersFolder, playerFileName);

    //     try {
    //         config.save(playerFile);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    private void copyDefaultFile(String fileName) {
        File targetFile = new File(pluginFolder, fileName);
        try (InputStream defaultStream = plugin.getResource(fileName)) {
            if (defaultStream != null) {
                Files.copy(defaultStream, targetFile.toPath());
                plugin.getLogger().info(fileName + " has been copied to the plugin folder.");
            } else {
                plugin.getLogger().warning(fileName + " not found in resources! Creating an empty file.");
                targetFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

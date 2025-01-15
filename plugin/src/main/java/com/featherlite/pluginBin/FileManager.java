// FileManager.java
package com.featherlite.pluginBin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

public class FileManager {
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> pluginConfigs = new HashMap<>();
    private final boolean isDebuggerOn;

    public FileManager(JavaPlugin plugin, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.isDebuggerOn = isDebuggerOn;
    }


    /**
     * Loads active item category files specified in config.yml.
     * Ensures the items folder exists and copies missing files from the plugin's resources.
     */
    public void loadActiveItemCategories() {
        // Get the list of active item categories from config.yml
        List<String> activeCategories = plugin.getConfig().getStringList("active-item-categories");

        if (activeCategories.isEmpty()) {
            plugin.getLogger().warning("No active item categories specified in config.yml.");
            return;
        }

        // Ensure the items directory within the plugin's data folder exists
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            if (itemsFolder.mkdirs()) {
                if (isDebuggerOn) {plugin.getLogger().info("Created items directory in plugin data folder.");}
            } else {
                plugin.getLogger().severe("Failed to create items directory!");
                return;
            }
        }

        // Loop through each specified category and attempt to load or copy the file
        for (String fileName : activeCategories) {
            File itemFile = new File(itemsFolder, fileName);

            if (!itemFile.exists()) {
                if (isDebuggerOn) {plugin.getLogger().info("File not found: " + fileName + ". Attempting to copy default resource.");}
                copyDefaultResourceIfAbsent("items/" + fileName, itemFile);
            } else {
                if (isDebuggerOn) {plugin.getLogger().info("Loading item file: " + fileName);}
                loadItemFile(itemFile);
            }
        }
    }

    /**
     * Loads an individual item configuration file.
     *
     * @param file The item file to load.
     */
    private void loadItemFile(File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            if (isDebuggerOn) {plugin.getLogger().info("Successfully loaded " + file.getName());}
            // Here you would handle the file content as needed for your plugin
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load " + file.getName());
            e.printStackTrace();
        }
    }

    /**
     * Copies a default resource from the JAR to the plugin's data folder if it doesn't exist.
     *
     * @param resourcePath   The path of the resource in the JAR (e.g., "items/armor.yml").
     * @param destinationFile The destination file in the plugin's data folder.
     */
    private void copyDefaultResourceIfAbsent(String resourcePath, File destinationFile) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) {
                plugin.getLogger().warning("Resource not found in JAR: " + resourcePath);
                return;
            }

            // Copy the resource if it exists in the JAR and not on disk
            Files.copy(in, destinationFile.toPath());
            if (isDebuggerOn) {plugin.getLogger().info("Copied default file: " + destinationFile.getName());}
        } catch (IOException e) {
            plugin.getLogger().severe("Error copying default file " + destinationFile.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public FileConfiguration loadConfig(String pluginName, String fileName) {
        // Load or retrieve the configuration for a specific plugin
        String key = pluginName + ":" + fileName;
        if (!pluginConfigs.containsKey(key)) {
            File configFile = new File(plugin.getDataFolder().getParentFile(), pluginName + "/" + fileName);
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            pluginConfigs.put(key, config);
        }
        return pluginConfigs.get(key);
    }

    public void saveConfig(FileConfiguration config, String filePath) {
        File configFile = new File(plugin.getDataFolder().getParentFile(), filePath);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving configuration to file: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> parseYAML(File file) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            result.putAll(config.getValues(true)); // Get all keys deeply
        } catch (Exception e) {
            plugin.getLogger().severe("Error parsing YAML file: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    // Recursively reads directories for .yml files and parses them into nested maps
    public String readYamlFileAsString(File file) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return new String(fileBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to read YAML file: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean writeYamlFileFromString(File file, String yamlContent) {
        try {
            Files.write(file.toPath(), yamlContent.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to write YAML file: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // New method to read all .yml files in a directory and return their contents as a map of filename to raw string
    public Map<String, String> readAllYamlFilesInDirectory(File dir) {
        Map<String, String> yamlFilesContent = new LinkedHashMap<>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".yml")) {
                    String content = readYamlFileAsString(file);
                    if (content != null) {
                        yamlFilesContent.put(file.getName(), content);
                    } else {
                        plugin.getLogger().warning("Failed to read YAML file: " + file.getName());
                    }
                }
            }
        } else {
            plugin.getLogger().warning("Provided file is not a directory: " + dir.getName());
        }
        return yamlFilesContent;
    }
}

package com.featherlite.pluginBin.chat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class ChatManager {

    private final JavaPlugin plugin;
    private FileConfiguration prefixesConfig;
    private FileConfiguration bannedWordsConfig;

    private final Map<String, PrefixData> permissionPrefixes = new HashMap<>();
    private List<String> bannedWords;

    public ChatManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize resource files
        saveResourceIfMissing("chat/prefixes.yml");
        saveResourceIfMissing("chat/banned-words.yml");

        // Load configuration files
        loadConfigs();
    }

    /**
     * Save a resource file if it doesn't already exist.
     */
    private void saveResourceIfMissing(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            plugin.getLogger().info(fileName + " has been created.");
        }
    }

    /**
     * Loads prefixes.yml and banned-words.yml, and caches their data.
     */
    private void loadConfigs() {
        prefixesConfig = loadYamlConfig("chat/prefixes.yml");
        bannedWordsConfig = loadYamlConfig("chat/banned-words.yml");

        // Load prefixes
        permissionPrefixes.clear();
        Set<String> keys = prefixesConfig.getConfigurationSection("prefixes").getKeys(false);
        for (String key : keys) {
            String permissionGroup = prefixesConfig.getString("prefixes." + key + ".permission-group");
            String tag = prefixesConfig.getString("prefixes." + key + ".tag");
            int weight = prefixesConfig.getInt("prefixes." + key + ".weight", 1); // Default weight = 1
            permissionPrefixes.put(permissionGroup, new PrefixData(tag, weight));
        }

        // Load banned words
        bannedWords = bannedWordsConfig.getStringList("banned-words");
    }

    /**
     * Loads a YAML configuration file.
     */
    private FileConfiguration loadYamlConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Gets the highest-weighted prefix for a list of permission groups.
     */
    public String getHighestWeightedPrefix(List<String> playerGroups) {
        PrefixData highest = null;

        for (String group : playerGroups) {
            PrefixData data = permissionPrefixes.get(group);
            if (data != null) {
                plugin.getLogger().info("Checking group: " + group + ", Tag: " + data.tag + ", Weight: " + data.weight);
                if (highest == null || data.weight > highest.weight) {
                    highest = data;
                }
            }
        }

        if (highest == null) {
            plugin.getLogger().info("No valid prefix found for player groups: " + playerGroups);
        }

        return (highest != null) ? highest.tag : ""; // Default to an empty string if no prefix is found
    }

    /**
     * Returns the list of banned words.
     */
    public List<String> getBannedWords() {
        return bannedWords;
    }

    /**
     * Inner class to store prefix data.
     */
    private static class PrefixData {
        String tag;
        int weight;

        PrefixData(String tag, int weight) {
            this.tag = tag;
            this.weight = weight;
        }
    }
}

package com.featherlite.pluginBin.essentials;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.featherlite.pluginBin.economy.EconomyManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataManager {
    private final File playerDataFolder;

    public PlayerDataManager(Plugin plugin, String subFolder) {
        // Set up the player data folder (e.g., "data/essentials")
        this.playerDataFolder = new File(plugin.getDataFolder(), subFolder);
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    /**
     * Retrieve a player's data file configuration by Player object.
     */
    public FileConfiguration getPlayerData(Player player) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        // New file format: UserName-UUID.yml
        File playerFile = new File(playerDataFolder, playerName + "-" + playerId + ".yml");

        if (!playerFile.exists()) {
            createPlayerFile(player, playerFile);
        }

        return YamlConfiguration.loadConfiguration(playerFile);
    }

    /**
     * Retrieve a player's data file configuration by UUID (legacy support).
     */
    public FileConfiguration getPlayerData(UUID playerId) {
        Player player = org.bukkit.Bukkit.getPlayer(playerId);
        if (player != null) {
            return getPlayerData(player); // Use Player object if available
        }

        // Fallback: Cannot retrieve player name, use legacy format (UUID.yml)
        File playerFile = new File(playerDataFolder, playerId.toString() + ".yml");
        if (!playerFile.exists()) {
            createPlayerFileFallback(playerFile, playerId);
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    /**
     * Save the player's data file configuration.
     */
    public void savePlayerData(Player player, FileConfiguration data) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();
        File playerFile = new File(playerDataFolder, playerName + "-" + playerId + ".yml");

        try {
            data.save(playerFile);
        } catch (IOException e) {
            System.err.println("Failed to save data for player: " + playerName + " (" + playerId + ")");
            e.printStackTrace();
        }
    }

    /**
     * Update a single key-value pair in a player's data.
     */
    public void updatePlayerData(Player player, String key, Object value) {
        FileConfiguration data = getPlayerData(player);
        data.set(key, value);
        savePlayerData(player, data);
    }

    /**
     * Update multiple key-value pairs in a player's data.
     */
    public void updatePlayerDataBatch(Player player, Map<String, Object> updates) {
        FileConfiguration data = getPlayerData(player);
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            data.set(entry.getKey(), entry.getValue());
        }
        savePlayerData(player, data);
    }

    /**
     * Retrieve a list of UUIDs from a player's data.
     */
    public List<UUID> getUUIDList(Player player, String key) {
        FileConfiguration data = getPlayerData(player);
        List<String> rawList = data.getStringList(key); // Use getStringList for safer type handling
        return rawList.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    /**
     * Save a list of UUIDs to a player's data.
     */
    public void setUUIDList(Player player, String key, List<UUID> uuidList) {
        List<String> stringList = uuidList.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());
        updatePlayerData(player, key, stringList);
    }

    /**
     * Create a new data file for a player.
     */
    private void createPlayerFile(Player player, File playerFile) {
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        try {
            if (playerFile.createNewFile()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

                // Initialize default player data
                config.set("user-stats.UUID", playerId.toString());
                config.set("user-stats.user-name", playerName);

                // Initialize balances with starting currency amount
                // Retrieve starting currency amount from the config
                FileConfiguration mainConfig = player.getServer().getPluginManager().getPlugin("FeatherLite-Core").getConfig();
                double startingCurrency = mainConfig.getDouble("starting-currency-amount", 0.0);
                String defaultCurrency = mainConfig.getString("currency-name", "Dollars");
                config.set("balances." + defaultCurrency, startingCurrency);

                savePlayerData(player, config);
            }
        } catch (IOException e) {
            System.err.println("Failed to create data file for player: " + playerName + " (" + playerId + ")");
            e.printStackTrace();
        }
    }

    /**
     * Create a new data file for a player using only their UUID (fallback for legacy cases).
     */
    private void createPlayerFileFallback(File playerFile, UUID playerId) {
        try {
            if (playerFile.createNewFile()) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
    
                // Initialize default player data
                config.set("user-stats.UUID", playerId.toString());
                config.set("user-stats.user-name", "Unknown");
    
                // Initialize balances with starting currency amount
                // Retrieve starting currency amount from the config
                FileConfiguration mainConfig = Bukkit.getPluginManager().getPlugin("FeatherLite-Core").getConfig();
                double startingCurrency = mainConfig.getDouble("starting-currency-amount", 0.0);
                String defaultCurrency = mainConfig.getString("currency-name", "Dollars");
                config.set("balances." + defaultCurrency, startingCurrency);
    
                config.save(playerFile);
            }
        } catch (IOException e) {
            System.err.println("Failed to create fallback data file for player UUID: " + playerId);
            e.printStackTrace();
        }
    }

    
}

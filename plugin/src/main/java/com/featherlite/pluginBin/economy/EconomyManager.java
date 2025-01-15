package com.featherlite.pluginBin.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.featherlite.pluginBin.essentials.PlayerDataManager;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class EconomyManager {
    private final Plugin plugin;
    // private final File economyFolder;
    private final File playerDataFolder;
    private final String defaultCurrency;
    private final String currencyPrefix;
    private final double startingCurrencyAmount;
    private final PlayerDataManager playerDataManager;

    public EconomyManager(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;

        // Read the currency name, prefix, and starting amount from the configuration
        FileConfiguration config = plugin.getConfig();
        this.defaultCurrency = config.getString("currency-name", "Dollars");
        this.currencyPrefix = config.getString("currency-prefix", "$");
        this.startingCurrencyAmount = config.getDouble("starting-currency-amount", 0.0);

        this.playerDataManager = playerDataManager;

        this.playerDataFolder = new File(plugin.getDataFolder(), "player_data");
        // Ensure the economy data folder exists
        // this.economyFolder = new File(plugin.getDataFolder(), "data/economy");
        // if (!economyFolder.exists()) {
        //     economyFolder.mkdirs();
        // }
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getCurrencyPrefix() {
        return currencyPrefix;
    }

    // Retrieve a player's balance for the default currency
    public double getBalance(UUID playerId) {
        Player target = Bukkit.getPlayer(playerId);

        return getBalance(target, defaultCurrency);
    }

    // Retrieve a player's balance for a specific currency
    public double getBalance(Player player, String currency) {
        FileConfiguration data = playerDataManager.getPlayerData(player);
        return data.getDouble("balances." + currency, 0.0);
    }

    // Add to a player's balance
    public void deposit(UUID playerId, double amount) {
        addBalance(playerId, defaultCurrency, amount);
    }

    public void addBalance(UUID playerId, String currency, double amount) {
        Player target = Bukkit.getPlayer(playerId);

        if (amount <= 0) return;
        FileConfiguration playerData = playerDataManager.getPlayerData(playerId);
        double newBalance = getBalance(target, currency) + amount;
        playerData.set("balances." + currency, newBalance);
        playerDataManager.savePlayerData(target, playerData);
    }

    // Subtract from a player's balance
    public boolean withdraw(UUID playerId, double amount) {
        return subtractBalance(playerId, defaultCurrency, amount);
    }

    public boolean subtractBalance(UUID playerId, String currency, double amount) {
        Player target = Bukkit.getPlayer(playerId);
        if (amount <= 0) return false;
        FileConfiguration playerData = playerDataManager.getPlayerData(target);
        double currentBalance = getBalance(target, currency);

        if (currentBalance < amount) {
            return false;
        }

        double newBalance = currentBalance - amount;
        playerData.set("balances." + currency, newBalance);
        playerDataManager.savePlayerData(target, playerData);
        return true;
    }

    // Set a player's balance
    public void setBalance(UUID playerId, double amount) {
        setBalance(playerId, defaultCurrency, amount);
    }

    public void setBalance(UUID playerId, String currency, double amount) {
        Player target = Bukkit.getPlayer(playerId);
        
        if (amount < 0) return;
        FileConfiguration playerData = playerDataManager.getPlayerData(target);
        playerData.set("balances." + currency, amount);
        playerDataManager.savePlayerData(target, playerData);
    }

    // Get a leaderboard of top balances (baltop)
    public List<Map.Entry<OfflinePlayer, Double>> getTopBalances(String currency) {
        Map<OfflinePlayer, Double> balances = new HashMap<>();
    
        for (File file : playerDataFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                try {
                    // Load the file's content
                    FileConfiguration data = YamlConfiguration.loadConfiguration(file);
                    
                    // Retrieve the UUID from the file
                    String uuidString = data.getString("user-stats.UUID");
                    if (uuidString == null) {
                        Bukkit.getLogger().warning("Missing UUID in file: " + file.getName());
                        continue;
                    }
    
                    UUID playerId = UUID.fromString(uuidString); // Safely parse the UUID
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
    
                    // Retrieve the balance for the specified currency
                    double balance = data.getDouble("balances." + currency, 0.0);
                    balances.put(player, balance);
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Invalid UUID in file: " + file.getName());
                }
            }
        }
    
        // Sort by balance in descending order
        return balances.entrySet().stream()
                .sorted(Map.Entry.<OfflinePlayer, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
    

}

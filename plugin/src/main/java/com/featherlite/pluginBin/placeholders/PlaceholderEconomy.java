package com.featherlite.pluginBin.placeholders;

import com.featherlite.pluginBin.economy.EconomyManager;
import org.bukkit.entity.Player;

public class PlaceholderEconomy {
    private static EconomyManager economyManager;

    // Method to set the EconomyManager (called during plugin setup)
    public static void setEconomyManager(EconomyManager manager) {
        economyManager = manager;
    }

    // Placeholder: <player_balance>
    public static String getCurrentBalance(Player player) {
        if (economyManager == null) {
            return "Economy Disabled";
        }
        double balance = economyManager.getBalance(player.getUniqueId());
        String currencyPrefix = economyManager.getCurrencyPrefix();
        return currencyPrefix + balance;
    }

    // Placeholder: <specific_currency_balance>
    public static String getSpecificCurrencyBalance(Player player, String currency) {
        if (economyManager == null) {
            return "Economy Disabled";
        }
        double balance = economyManager.getBalance(player, currency);
        return balance + " " + currency;
    }

    // Placeholder: <top_balance>
    public static String getTopBalance(Player player) {
        if (economyManager == null) {
            return "Economy Disabled";
        }
        return economyManager.getTopBalances(economyManager.getDefaultCurrency())
                .stream()
                .findFirst()
                .map(entry -> entry.getKey().getName() + ": " + economyManager.getCurrencyPrefix() + entry.getValue())
                .orElse("No data");
    }
}

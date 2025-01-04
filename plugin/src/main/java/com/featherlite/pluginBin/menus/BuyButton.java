package com.featherlite.pluginBin.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.type.Cocoa;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.featherlite.pluginBin.economy.EconomyManager;

public class BuyButton implements MenuButton {
    private final ItemStack icon; // Icon displayed in the inventory
    private final String currency; // e.g., "GOLD_INGOT" or "eco"
    private final double cost;
    private final String command; // Command to execute
    private final EconomyManager economyManager;
    private final JavaPlugin plugin;

    public BuyButton(ItemStack icon, String currency, double cost, String command, EconomyManager economyManager, JavaPlugin plugin) {
        this.icon = icon;
        this.currency = currency;
        this.cost = cost;
        this.command = command;
        this.economyManager = economyManager;
        this.plugin = plugin;
    }

    @Override
    public ItemStack getIcon() {
        return icon; // Return the icon for visual representation
    }

    @Override
    public void onClick(Player player) {
        if (!hasEnoughCurrency(player)) {
            player.sendMessage("§cYou don't have enough " + currency + " to make this purchase!");
            return;
        }

        // Execute the command
        String parsedCommand = command.replace("<player>", player.getName());
        plugin.getLogger().info("Executing command as console: " + parsedCommand);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        player.sendMessage("§aPurchase successful!");

        // Deduct the cost
        deductCurrency(player);
    }

    private boolean hasEnoughCurrency(Player player) {
        if (currency.equalsIgnoreCase("eco")) {
            return economyManager.getBalance(player.getUniqueId()) >= cost;
        } else {
            Material currencyMaterial;
            try {
                currencyMaterial = Material.valueOf(currency.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid currency type: " + currency);
                return false;
            }

            return countItems(player, currencyMaterial) >= cost;
        }
    }

    private void deductCurrency(Player player) {
        if (currency.equalsIgnoreCase("eco")) {
            economyManager.withdraw(player.getUniqueId(), cost);
        } else {
            Material currencyMaterial = Material.valueOf(currency.toUpperCase());
            removeItems(player, new ItemStack(currencyMaterial), (int) cost);
        }
    }

    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, ItemStack item, int quantity) {
        int remaining = quantity;

        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.isSimilar(item)) {
                if (stack.getAmount() > remaining) {
                    stack.setAmount(stack.getAmount() - remaining);
                    return;
                } else {
                    remaining -= stack.getAmount();
                    player.getInventory().remove(stack);
                }
            }

            if (remaining <= 0) {
                return;
            }
        }
    }
    
}

package com.featherlite.pluginBin.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import com.featherlite.pluginBin.economy.EconomyManager;


public class BuyButton implements MenuButton {
    private final ItemStack icon;
    private final String currency;
    private final double cost;
    private final String vanillaMaterial;
    private final int vanillaAmount;
    private final String command;
    private final EconomyManager economyManager;
    private final JavaPlugin plugin;

    public BuyButton(ItemStack icon, String currency, double cost, String vanillaMaterial, int vanillaAmount, String command, EconomyManager economyManager, JavaPlugin plugin) {
        this.icon = icon;
        this.currency = currency;
        this.cost = cost;
        this.vanillaMaterial = vanillaMaterial;
        this.vanillaAmount = vanillaAmount;
        this.command = command;
        this.economyManager = economyManager;
        this.plugin = plugin;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public void onClick(Player player) {
        if (!hasEnoughCurrency(player)) {
            player.sendMessage("§cYou don't have enough " + currency + " to make this purchase!");
            return;
        }

        // Execute Vanilla Item Giving
        if (vanillaMaterial != null) {
            Material material;
            try {
                material = Material.valueOf(vanillaMaterial.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cInvalid material type: " + vanillaMaterial);
                return;
            }
            ItemStack item = new ItemStack(material, vanillaAmount);
            player.getInventory().addItem(item);
            player.sendMessage("§aPurchase successful! You received " + vanillaAmount + " " + material.name().toLowerCase() + "!");
        }

        // Execute Command
        if (command != null && !command.isEmpty()) {
            String parsedCommand = command.replace("<player>", player.getName());
            plugin.getLogger().info("Executing command as console: " + parsedCommand);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
        }

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

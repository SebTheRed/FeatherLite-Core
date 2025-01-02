package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.utils.InventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommands {
    private final InventoryManager inventoryManager;

    public InventoryCommands(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public boolean handleInventoryCommands(Player sender, String[] args) {

        if (args.length == 0) {
            sender.sendMessage("§cUsage: /restoreinventory <player>");
            return true;
        }

        // Check if the target player exists
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found or is not online.");
            return true;
        }

        // Restore the inventory of the target player
        if (inventoryManager.hasStoredInventory(target)) {
            inventoryManager.restoreInventory(target);
            sender.sendMessage("§aRestored inventory for " + target.getName() + ".");
        } else {
            sender.sendMessage("§cNo saved inventory found for " + target.getName() + ".");
        }

        return true;
    }
}

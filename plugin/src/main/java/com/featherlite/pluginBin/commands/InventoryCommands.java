package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.utils.InventoryManager;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryCommands {
    private final InventoryManager inventoryManager;

    public InventoryCommands(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    public boolean handleInventoryCommands(CommandSender sender, String[] args, boolean isPlayer) {

        if (isPlayer && !(sender.hasPermission("core.restoreinventory") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }

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

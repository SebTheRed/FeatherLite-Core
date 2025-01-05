package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.items.ItemManager;
import com.featherlite.pluginBin.items.UIManager;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemCommands {
    private final UIManager uiManager;
    private final ItemManager itemManager;
    private final JavaPlugin plugin;

    public ItemCommands(UIManager uiManager, ItemManager itemManager, JavaPlugin plugin) {
        this.uiManager = uiManager;
        this.itemManager = itemManager;
        this.plugin = plugin;
    }

    public boolean handleItemCommands(CommandSender sender, String[] args, boolean isPlayer, JavaPlugin plugin) {

        Player player = (isPlayer ? (Player) sender : null);

        if (player != null && !player.hasPermission("feathercore.itemui")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player != null && player.hasPermission("feathercore.itemui.reload")) {  // Separate permission for reloading
                itemManager.reloadItems(uiManager);
                sender.sendMessage("Items have been reloaded from the configuration.");
            } else {
                sender.sendMessage("You do not have permission to reload items.");
            }
        } else {
            // Open the Item UI if no arguments are provided
            if (isPlayer) {
                uiManager.openItemUI(player);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can open the /items menu!");
            }
        }
        return true;
    }
}

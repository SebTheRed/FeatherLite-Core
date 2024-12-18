package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.items.ItemManager;
import com.featherlite.pluginBin.items.UIManager;

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

    public boolean handleItemCommands(Player sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("feathercore.itemui")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("feathercore.itemui.reload")) {  // Separate permission for reloading
                itemManager.reloadItems(sender, uiManager);
                player.sendMessage("Items have been reloaded from the configuration.");
            } else {
                player.sendMessage("You do not have permission to reload items.");
            }
        } else {
            // Open the Item UI if no arguments are provided
            uiManager.openItemUI(player);
        }
        return true;
    }
}

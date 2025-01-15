package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.items.ItemManager;
import com.featherlite.pluginBin.items.UIManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemCommands implements TabCompleter {
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

        if (args.length == 0) {
            // Open the Item UI if no arguments are provided
            if (isPlayer) {
                if (isPlayer && !sender.hasPermission("core.items.menu")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                uiManager.openItemUI(player);
            } else {
                sender.sendMessage(ChatColor.RED + "Only players can open the /items menu!");
            }
            return true;
        }

        // Handle "reload" subcommand
        if (args[0].equalsIgnoreCase("reload")) {
            if (player.hasPermission("core.items.reload")) {  // Separate permission for reloading
                itemManager.reloadItems(uiManager);
                sender.sendMessage("Items have been reloaded from the configuration.");
            } else {
                sender.sendMessage("You do not have permission to reload items.");
            }
            return true;
        }

        // Handle "give" subcommand: /item give <category> <item_name> [player_name]
        if (args[0].equalsIgnoreCase("give")) {

            if (isPlayer && !sender.hasPermission("core.items.give")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /item give <category> <item_name> [player_name]");
                return true;
            }

            String category = args[1];
            String itemName = args[2];

            // Check if the item exists in the specified category
            ItemStack item = itemManager.getItem(category, itemName);
            if (item == null) {
                sender.sendMessage(ChatColor.RED + "Item '" + itemName + "' not found in category '" + category + "'.");
                return true;
            }

            // Determine target player (default to the sender if they're a player)
            Player targetPlayer = player;
            if (args.length == 4) {
                targetPlayer = Bukkit.getPlayer(args[3]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[3]);
                    return true;
                }
            }

            // Ensure the target player is valid
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "You must specify a valid target player.");
                return true;
            }

            // Give the item to the target player
            targetPlayer.getInventory().addItem(item.clone());
            sender.sendMessage(ChatColor.GREEN + "Gave " + itemName + " to " + targetPlayer.getName() + ".");
            if (!targetPlayer.equals(sender)) {
                targetPlayer.sendMessage(ChatColor.GREEN + "You received " + itemName + " from " + sender.getName() + ".");
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /item give or /item reload.");
        return true;
    }

    // Tab completer for the /item command
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!command.getName().equalsIgnoreCase("item")) {
            return suggestions;
        }

        // Handle top-level arguments
        if (args.length == 1) {
            suggestions.add("give");
            suggestions.add("reload");
            return filterSuggestions(suggestions, args[0]);
        }

        // Handle "give" subcommand
        if (args[0].equalsIgnoreCase("give")) {
            // Second argument: category
            if (args.length == 2) {
                suggestions.addAll(itemManager.getCategories());
                return filterSuggestions(suggestions, args[1]);
            }

            // Third argument: item name within the selected category
            if (args.length == 3) {
                String category = args[1];
                if (itemManager.getCategories().contains(category)) {
                    suggestions.addAll(itemManager.getItemsInCategory(category).keySet());
                }
                return filterSuggestions(suggestions, args[2]);
            }

            // Fourth argument: player name
            if (args.length == 4) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
                return filterSuggestions(suggestions, args[3]);
            }
        }

        return suggestions;
    }

    /**
     * Filters suggestions based on the current argument being typed.
     *
     * @param suggestions the list of possible suggestions
     * @param current     the current argument being typed
     * @return the filtered list of suggestions
     */
    private List<String> filterSuggestions(List<String> suggestions, String current) {
        if (current == null || current.isEmpty()) {
            return suggestions;
        }
        String lowerCurrent = current.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(lowerCurrent)) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }
}

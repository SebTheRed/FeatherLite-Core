package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.admin.AdminManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.NumberFormatException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class AdminCommands implements TabCompleter {
    private final AdminManager adminManager;

    public AdminCommands(AdminManager adminManager) {
        this.adminManager = adminManager;
    }

    public boolean handleAdminCommands(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use admin commands.");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {

            case "enchant":
                if (args.length < 2) {
                    player.sendMessage("Usage: /enchant <enchantment> <level>");
                    return true;
                }
                return adminManager.enchantItem(player, args[0], Integer.parseInt(args[1]));

            case "exp":
                if (args.length < 2) {
                    player.sendMessage("Usage: /exp <give|set|view> <amount>");
                    return true;
                }
                handleExpCommand(player, args);
                return true;

            case "give":
                if (args.length < 2) {
                    player.sendMessage("Usage: /give <player> <item> [amount]");
                    return true;
                }
                return handleGiveCommand(player, args);

            case "kill":
                if (args.length < 1) {
                    player.sendMessage("Usage: /kill <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
                adminManager.killPlayer(target);
                player.sendMessage("Killed " + target.getName());
                return true;

            case "sudo":
                if (args.length < 2) {
                    player.sendMessage("Usage: /sudo <player> <command>");
                    return true;
                }
                Player sudoTarget = Bukkit.getPlayer(args[0]);
                if (sudoTarget == null) {
                    player.sendMessage("Player not found.");
                    return true;
                }
                String commandToRun = String.join(" ", args).substring(args[0].length() + 1);
                adminManager.sudoPlayer(player, sudoTarget, commandToRun);
                return true;

            case "weather":
                if (args.length < 1) {
                    player.sendMessage("Usage: /weather <clear|rain|thunder>");
                    return true;
                }
                adminManager.setWeather(player.getWorld(), args[0]);
                player.sendMessage("Weather updated to " + args[0]);
                return true;

            case "god":
                boolean enabled = adminManager.toggleGodMode(player);
                player.sendMessage(enabled 
                    ? ChatColor.GREEN + "You are now invulnerable!" 
                    : ChatColor.RED + "You are no longer invulnerable!");
                return true;
                
            case "time":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /time <set|add> <value>");
                    return true;
                }
            
                try {
                    String action = args[0].toLowerCase(); // e.g., "set" or "add"
                    String value = args[1];
            
                    switch (action) {
                        case "set":
                            adminManager.setTime(player.getWorld(), value);
                            player.sendMessage(ChatColor.GREEN + "Time set to " + value + ".");
                            break;
            
                        case "add":
                            // Add tick values (must be numeric for "add")
                            long additionalTicks = Long.parseLong(value);
                            long newTime = (player.getWorld().getTime() + additionalTicks) % 24000;
                            adminManager.setTime(player.getWorld(), String.valueOf(newTime));
                            player.sendMessage(ChatColor.GREEN + "Added " + additionalTicks + " ticks. Current time: " + newTime);
                            break;
            
                        default:
                            player.sendMessage(ChatColor.RED + "Invalid action. Use 'set' or 'add'.");
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                }
            
                return true;
            
            case "killall":
            case "remove":
                if (!player.hasPermission("core.killall")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }
            
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /killall <monsters|entities|boats|minecarts|players|drops|arrows|mobs> [radius|world]");
                    return true;
                }
            
                String targetType = args[0];
                String scope = args.length > 1 ? args[1] : "world";
            
                adminManager.killAll(player, targetType, scope);
                return true;
            default:
                player.sendMessage("Unknown admin command.");
                return true;
        }
    }

    private void handleExpCommand(Player player, String[] args) {
        String action = args[0].toLowerCase();
        int amount = Integer.parseInt(args[1]);

        switch (action) {
            case "give":
                adminManager.giveExp(player, amount);
                break;
            case "set":
                adminManager.setExp(player, amount);
                break;
            // case "view":
            //     adminManager.viewExp(player);
            //     break;
            default:
                player.sendMessage("Unknown sub-command for /exp.");
        }
    }

    private boolean handleGiveCommand(Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return true;
        }

        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            player.sendMessage("Invalid item.");
            return true;
        }

        int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        return adminManager.giveItem(player, target, material, amount);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        List<String> suggestions = new ArrayList<>();
        switch (alias.toLowerCase()) {
            case "repair":
                // No arguments for repair
                break;
            case "enchant":
                if (args.length == 1) {
                    for (Enchantment enchant : Enchantment.values()) {
                        suggestions.add(enchant.getKey().getKey());
                    }
                } else if (args.length == 2) {
                    suggestions.add("<level>");
                }
                break;
            case "exp":
                if (args.length == 1) {
                    suggestions.add("give");
                    suggestions.add("set");
                } else if (args.length > 1) {
                    suggestions.add("<amount>");
                }
                break;
            case "give":
            case "kill":
            case "sudo":
                if (args.length == 1) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(player.getName());
                    }
                } else if (args.length == 2 && alias.equalsIgnoreCase("give")) {
                    for (Material material : Material.values()) {
                        suggestions.add(material.name().toLowerCase());
                    }
                }
                break;
            case "weather":
                if (args.length == 1) {
                    suggestions.add("clear");
                    suggestions.add("rain");
                    suggestions.add("thunder");
                }
                break;
            case "time":
                if (args.length == 1) {
                    // Suggest main actions
                    suggestions.add("set");
                    suggestions.add("add");
                } else if (args.length == 2) {
                    // Check if the first argument is "set" or "add"
                    if (args[0].equalsIgnoreCase("set")) {
                        suggestions.add("morning");
                        suggestions.add("noon");
                        suggestions.add("night");
                        suggestions.add("midnight");
                        suggestions.add("<tick_value>"); // Placeholder for numeric ticks
                    } else if (args[0].equalsIgnoreCase("add")) {
                        suggestions.add("<tick_value>"); // Only numeric values are valid for "add"
                    }
                }
                break;
            case "killall":
            case "remove":
                if (args.length == 1) {
                    suggestions.addAll(List.of("monsters", "entities", "boats", "minecarts", "players", "drops", "arrows", "mobs"));
                } else if (args.length == 2) {
                    suggestions.add("<radius>");
                    suggestions.add("world");
                }
                break;
        }
        return suggestions;
    }

}

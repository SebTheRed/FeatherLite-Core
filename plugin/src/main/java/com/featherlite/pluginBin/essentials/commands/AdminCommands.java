package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.admin.AdminManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class AdminCommands implements TabCompleter {
    private final AdminManager adminManager;

    public AdminCommands(AdminManager adminManager) {
        this.adminManager = adminManager;
    }

    public boolean handleAdminCommands(CommandSender sender, Command command, String label, String[] args, boolean isPlayer) {

        Player player = (isPlayer ? (Player) sender : null);

        switch (label.toLowerCase()) {

            case "enchant":
                if (isPlayer && !(sender.hasPermission("core.enchant") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 2) {
                    if (!isPlayer) {sender.sendMessage("Only players can type the /enchant command!"); return true;}
                    sender.sendMessage("Usage: /enchant <enchantment> <level>");
                    return true;
                }
                return adminManager.enchantItem(player, args[0], Integer.parseInt(args[1]));

            case "exp":
                if (isPlayer && !(sender.hasPermission("core.exp") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /exp <give|set|view> <player> <amount>");
                    return true;
                }
                Player expTarget = Bukkit.getPlayer(args[1]);
                if (expTarget == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                handleExpCommand(expTarget, args);
                return true;

            case "give":
                if (isPlayer && !(sender.hasPermission("core.give") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /give <player> <item> [amount]"); 
                    return true;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                return handleGiveCommand(sender, target, args);

            case "kill":
                if (isPlayer && !(sender.hasPermission("core.kill") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /kill <player>");
                    return true;
                }
                Player killTarget = Bukkit.getPlayer(args[0]);
                if (killTarget == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                adminManager.killPlayer(killTarget);
                sender.sendMessage("Killed " + killTarget.getName());
                return true;

            case "sudo":
                if (isPlayer && !(sender.hasPermission("core.sudo") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("Usage: /sudo <player> <command>");
                    return true;
                }
                Player sudoTarget = Bukkit.getPlayer(args[0]);
                if (sudoTarget == null) {
                    sender.sendMessage("Player not found.");
                    return true;
                }
                String commandToRun = String.join(" ", args).substring(args[0].length() + 1);
                adminManager.sudoPlayer(player, sudoTarget, commandToRun);
                return true;

            case "weather":
                if (isPlayer && !(sender.hasPermission("core.weather") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1 || args.length > 2) {
                    sender.sendMessage("Usage: /weather <clear|rain|thunder> [world]");
                    return true;
                }

                String weatherType = args[0].toLowerCase();
                World targetWorld;

                // Determine the target world
                if (args.length == 2) {
                    targetWorld = Bukkit.getWorld(args[1]);
                    if (targetWorld == null) {
                        sender.sendMessage(ChatColor.RED + "World not found: " + args[1]);
                        return true;
                    }
                } else {
                    // Default to player's world, or reject if console
                    if (!isPlayer) {
                        sender.sendMessage(ChatColor.RED + "Console must specify a world.");
                        return true;
                    }
                    targetWorld = player.getWorld();
                }

                // Update the weather
                adminManager.setWeather(targetWorld, weatherType);
                sender.sendMessage(ChatColor.GREEN + "Weather updated to " + weatherType + " in world " + targetWorld.getName() + ".");
                return true;


            case "god":
                if (isPlayer && !(sender.hasPermission("core.god") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage("Only players can type the /god command!"); return true;}
                boolean enabled = adminManager.toggleGodMode(player);
                player.sendMessage(enabled 
                    ? ChatColor.GREEN + "You are now invulnerable!" 
                    : ChatColor.RED + "You are no longer invulnerable!");
                return true;
                
            case "time":
                if (args.length < 2 || args.length > 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /time <set|add> <value> [world]");
                    return true;
                }
            
                try {
                    String action = args[0].toLowerCase(); // e.g., "set" or "add"
                    String value = args[1];
                    World targetTimeWorld;
            
                    // Determine the target world
                    if (args.length == 3) {
                        targetTimeWorld = Bukkit.getWorld(args[2]);
                        if (targetTimeWorld == null) {
                            sender.sendMessage(ChatColor.RED + "World not found: " + args[2]);
                            return true;
                        }
                    } else {
                        if (!isPlayer) {
                            sender.sendMessage(ChatColor.RED + "Console must specify a world.");
                            return true;
                        }
                        targetTimeWorld = player.getWorld();
                    }
            
                    // Handle time actions
                    switch (action) {
                        case "set":
                            adminManager.setTime(targetTimeWorld, value);
                            sender.sendMessage(ChatColor.GREEN + "Time set to " + value + " in world " + targetTimeWorld.getName() + ".");
                            break;
            
                        case "add":
                            long additionalTicks = Long.parseLong(value);
                            long newTime = (targetTimeWorld.getTime() + additionalTicks) % 24000;
                            adminManager.setTime(targetTimeWorld, String.valueOf(newTime));
                            sender.sendMessage(ChatColor.GREEN + "Added " + additionalTicks + " ticks. Current time: " + newTime + " in world " + targetTimeWorld.getName() + ".");
                            break;
            
                        default:
                            sender.sendMessage(ChatColor.RED + "Invalid action. Use 'set' or 'add'.");
                            break;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid number: " + args[1]);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + e.getMessage());
                }
                return true;
            
            
            case "killall":
            case "remove":
                if (isPlayer && !(sender.hasPermission("core.killall") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Usage: /killall <monsters|entities|boats|minecarts|players|drops|arrows|mobs> [world-name]");
                    return true;
                }
            
                String targetType = args[0];
                String scope = args.length > 1 ? args[1] : "world";
            
                adminManager.killAll(player, targetType, scope, isPlayer);
                return true;
            default:
                sender.sendMessage("Unknown admin command.");
                return true;
        }
    }

    private void handleExpCommand(Player player, String[] args) {
        String action = args[0].toLowerCase();
        int amount = Integer.parseInt(args[2]);

        switch (action) {
            case "give":
                adminManager.giveExp(player, amount);
                player.sendMessage(ChatColor.GREEN + "You have been given " + amount + " EXP!");
                break;
            case "set":
                adminManager.setExp(player, amount);
                player.sendMessage(ChatColor.GREEN + "Your EXP has been set to " + amount + "!");
                break;
            // case "view":
            //     adminManager.viewExp(player);
            //     break;
            default:
                break;
        }
    }

    private boolean handleGiveCommand(CommandSender sender, Player player, String[] args) {
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            sender.sendMessage("Invalid item.");
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
                        suggestions.add("day");
                        suggestions.add("noon");
                        suggestions.add("night");
                        suggestions.add("midnight");
                        suggestions.add("<tick_time>"); // Placeholder for numeric ticks
                    } else if (args[0].equalsIgnoreCase("add")) {
                        suggestions.add("<tick_time>"); // Only numeric values are valid for "add"
                    }
                }
                break;
            case "killall":
            case "remove":
                if (args.length == 1) {
                    suggestions.addAll(List.of("monsters", "displays", "entities", "boats", "minecarts", "players", "drops", "arrows", "mobs"));
                } else if (args.length == 2) {
                    suggestions.add("<radius>");
                    suggestions.add("world");
                }
                break;
        }
        return suggestions;
    }

}

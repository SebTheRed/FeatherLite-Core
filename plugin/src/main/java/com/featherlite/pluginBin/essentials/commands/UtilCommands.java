package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.util.UtilManager;
import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilCommands implements TabCompleter {
    private final UtilManager utilManager;
    private final PlayerDataManager playerDataManager;

    public UtilCommands(UtilManager utilManager, PlayerDataManager playerDataManager) {
        this.utilManager = utilManager;
        this.playerDataManager = playerDataManager;
    }

    public boolean handleUtilCommands(CommandSender sender, Command command, String label, String[] args, boolean isPlayer) {
        Player executor = isPlayer ? (Player) sender : null;
    
        // Get the target player (if specified)
        Player target = null;
        // Determine the target player
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[args.length - 1]); // Last argument might be a player name
            if (target == null && !isPlayer) {
                sender.sendMessage(ChatColor.RED + "Console must specify a valid target player.");
                return true;
            }
        }

        // If no target specified, default to the executor (if they're a player)
        if (target == null) {
            target = executor;
        }
        
        switch (label.toLowerCase()) {
            case "fly":
                if (isPlayer && !(sender.hasPermission("core.fly") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.toggleFlight(target != null ? target : executor);
            case "speed":
                if (isPlayer && !(sender.hasPermission("core.speed") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /speed <value> [player_name]");
                    return true;
                }
                return utilManager.setWalkSpeed(target != null ? target : executor, args[0]);
            case "flyspeed":
                if (isPlayer && !(sender.hasPermission("core.flyspeed") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /flyspeed <value> [player_name]");
                    return true;
                }
                return utilManager.setFlySpeed(target != null ? target : executor, args[0]);
            case "gamemode":
            case "gm":
                if (isPlayer && !(sender.hasPermission("core.gamemode") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /gamemode <survival|creative|spectator> [player_name]");
                    return true;
                }
                return utilManager.setGameMode(target != null ? target : executor, args[0]);
            case "heal":
                if (isPlayer && !(sender.hasPermission("core.heal") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.healPlayer(target != null ? target : executor, sender);
            case "feed":
                if (isPlayer && !(sender.hasPermission("core.feed") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.feedPlayer(target != null ? target : executor, sender);
            case "rest":
                if (isPlayer && !(sender.hasPermission("core.rest") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.restPlayer(target != null ? target : executor, sender);
            case "repair":
                if (isPlayer && !(sender.hasPermission("core.repair") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can /repair their own gear!"); return true;}
                return utilManager.repairItem(executor); // Repair remains executor-specific
            case "afk":
                if (isPlayer && !(sender.hasPermission("core.afk") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.toggleAFK(target != null ? target : executor, sender);
            case "enderchest":
            case "ec":
                if (isPlayer && !(sender.hasPermission("core.enderchest") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /ec in game!"); return true;}
                return utilManager.openEnderChest(target != null ? target : executor, sender);
            case "trash":
                if (isPlayer && !(sender.hasPermission("core.trash") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /trash in game!"); return true;}
                return utilManager.openTrash(executor); // Trash remains executor-specific
            case "top":
                if (isPlayer && !(sender.hasPermission("core.top") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /top in game!"); return true;}
                return utilManager.teleportToTop(executor); // Top remains executor-specific
            case "hat":
                if (isPlayer && !(sender.hasPermission("core.hat") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /hat in game!"); return true;}
                return utilManager.wearHat(executor); // Hat remains executor-specific
            case "nick":
            case "nickname":
                if (isPlayer && !(sender.hasPermission("core.nick") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /nick <desired_name> [player_name]");
                    return true;
                }
                return utilManager.setNickname(target != null ? target : executor, args, sender, playerDataManager);
            case "realname":
                if (isPlayer && !(sender.hasPermission("core.realname") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /realname <player_nickname>");
                    return true;
                }
                return utilManager.getRealName(executor, args);
            case "list":
                if (isPlayer && !(sender.hasPermission("core.list") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.listPlayers(sender);
            case "near":
                if (isPlayer && !(sender.hasPermission("core.near") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /near in game!"); return true;}
                if (args.length < 1) {
                    sender.sendMessage("Usage: /near <radius>");
                    return true;
                }
                return utilManager.nearPlayers(executor, args);
            case "getpos":
                if (isPlayer && !(sender.hasPermission("core.getpos") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.getPlayerPosition(sender, target);
            
            case "ping":
                if (isPlayer && !(sender.hasPermission("core.ping") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                return utilManager.pingPlayer(target != null ? target : executor, sender);
            case "seen":
                if (isPlayer && !(sender.hasPermission("core.seen") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (args.length < 1) {
                    sender.sendMessage("Usage: /seen <player_name>");
                    return true;
                }
                return utilManager.seenPlayer(sender, args);
    
            case "workbench":
            case "wb":
            case "craft":
                if (isPlayer && !(sender.hasPermission("core.workbench") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /craft in game!"); return true;}
                return utilManager.openWorkbench(executor); // Workbench remains executor-specific

            case "anvil":
                if (isPlayer && !(sender.hasPermission("core.anvil") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /anvil in game!"); return true;}
                return utilManager.openAnvil(executor); // Anvil remains executor-specific

            case "cartographytable":
                if (isPlayer && !(sender.hasPermission("core.cartographytable") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /cartographytable in game!"); return true;}
                return utilManager.openCartographyTable(executor); // Cartography remains executor-specific

            case "grindstone":
                if (isPlayer && !(sender.hasPermission("core.grindstone") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /grindstone in game!"); return true;}
                return utilManager.openGrindstone(executor); // Grindstone remains executor-specific

            case "loom":
                if (isPlayer && !(sender.hasPermission("core.loom") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /loom in game!"); return true;}
                return utilManager.openLoom(executor); // Loom remains executor-specific
    
            case "smithingtable":
            case "smithing":
                if (isPlayer && !(sender.hasPermission("core.smithing") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /smithingtable in game!"); return true;}
                return utilManager.openSmithingTable(executor); // Smithing remains executor-specific
    
            case "stonecutter":
                if (isPlayer && !(sender.hasPermission("core.stonecutter") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /stonecutter in game!"); return true;}
                return utilManager.openStonecutter(executor); // Stonecutter remains executor-specific
    
            case "ptime":
                if (isPlayer && !(sender.hasPermission("core.ptime") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /ptime in game!"); return true;}
                return utilManager.setPlayerTime(target != null ? target : executor, args);
    
            case "pweather":
                if (isPlayer && !(sender.hasPermission("core.pweather") || sender.isOp())) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
                    return true;
                }
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "Only players can use /pweather in game!"); return true;}
                return utilManager.setPlayerWeather(target != null ? target : executor, args);
    
            default:
                sender.sendMessage("Unknown utility command.");
                return true;
        }
    }
    

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
    
        Player player = (Player) sender;
        List<String> suggestions = new ArrayList<>();
    
        switch (alias.toLowerCase()) {
            case "gamemode":
            case "gm":
                if (player.hasPermission("core.gamemode") || player.isOp()) {
                    if (args.length == 1) {
                        suggestions.addAll(List.of("survival", "creative", "spectator"));
                    }
                    if (args.length == 2) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "speed":
            case "flyspeed":
                if ((alias.equalsIgnoreCase("speed") && player.hasPermission("core.speed")) ||
                    (alias.equalsIgnoreCase("flyspeed") && player.hasPermission("core.flyspeed")) || 
                    player.isOp()) {
                    if (args.length == 1) {
                        suggestions.addAll(List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"));
                    }
                    if (args.length == 2) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "near":
                if (player.hasPermission("core.near") || player.isOp()) {
                    if (args.length == 1) {
                        suggestions.add("<radius>");
                    }
                }
                break;
    
            case "nick":
            case "nickname":
                if (player.hasPermission("core.nick") || player.isOp()) {
                    if (args.length == 1) {
                        suggestions.add("<new nickname>");
                    }
                    if (args.length == 2) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "realname":
            case "seen":
            case "getpos":
                if ((alias.equalsIgnoreCase("realname") && player.hasPermission("core.realname")) ||
                    (alias.equalsIgnoreCase("seen") && player.hasPermission("core.seen")) ||
                    (alias.equalsIgnoreCase("getpos") && player.hasPermission("core.getpos")) ||
                    player.isOp()) {
                    if (args.length == 1) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "ptime":
                if (player.hasPermission("core.ptime") || player.isOp()) {
                    if (args.length == 1) {
                        suggestions.addAll(List.of("morning", "noon", "night", "<ticks>"));
                    }
                    if (args.length == 2) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "pweather":
                if (player.hasPermission("core.pweather") || player.isOp()) {
                    if (args.length == 1) {
                        suggestions.addAll(List.of("clear", "storm", "thunder"));
                    }
                    if (args.length == 2) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "fly":
                if (player.hasPermission("core.fly") || player.isOp()) {
                    if (args.length == 1) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "enderchest":
            case "ec":
                if (player.hasPermission("core.enderchest") || player.isOp()) {
                    if (args.length == 1) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "heal":
            case "feed":
            case "rest":
            case "afk":
            case "ping":
                if ((alias.equalsIgnoreCase("heal") && player.hasPermission("core.heal")) ||
                    (alias.equalsIgnoreCase("feed") && player.hasPermission("core.feed")) ||
                    (alias.equalsIgnoreCase("rest") && player.hasPermission("core.rest")) ||
                    (alias.equalsIgnoreCase("afk") && player.hasPermission("core.afk")) ||
                    (alias.equalsIgnoreCase("ping") && player.hasPermission("core.ping")) ||
                    player.isOp()) {
                    if (args.length == 1) {
                        Bukkit.getOnlinePlayers().forEach(online -> suggestions.add(online.getName()));
                    }
                }
                break;
    
            case "trash":
            case "top":
            case "hat":
                if ((alias.equalsIgnoreCase("trash") && player.hasPermission("core.trash")) ||
                    (alias.equalsIgnoreCase("top") && player.hasPermission("core.top")) ||
                    (alias.equalsIgnoreCase("hat") && player.hasPermission("core.hat")) ||
                    player.isOp()) {
                    // No specific arguments for these commands
                }
                break;
    
            case "list":
                if (player.hasPermission("core.list") || player.isOp()) {
                    // No specific arguments for /list
                }
                break;
    
            case "workbench":
            case "wb":
            case "craft":
            case "anvil":
            case "cartographytable":
            case "grindstone":
            case "loom":
            case "smithingtable":
            case "smithing":
            case "stonecutter":
                if ((alias.equalsIgnoreCase("workbench") && player.hasPermission("core.workbench")) ||
                    (alias.equalsIgnoreCase("anvil") && player.hasPermission("core.anvil")) ||
                    (alias.equalsIgnoreCase("cartographytable") && player.hasPermission("core.cartographytable")) ||
                    (alias.equalsIgnoreCase("grindstone") && player.hasPermission("core.grindstone")) ||
                    (alias.equalsIgnoreCase("loom") && player.hasPermission("core.loom")) ||
                    (alias.equalsIgnoreCase("smithingtable") && player.hasPermission("core.smithing")) ||
                    (alias.equalsIgnoreCase("stonecutter") && player.hasPermission("core.stonecutter")) ||
                    player.isOp()) {
                    // No specific arguments for these commands
                }
                break;
        }
    
        return suggestions;
    }
    
}

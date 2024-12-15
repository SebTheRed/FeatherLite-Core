package com.featherlite.pluginBin.essentials.commands;

import com.featherlite.pluginBin.essentials.util.UtilManager;
import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.Bukkit;
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

    public boolean handleUtilCommands(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use utility commands.");
            return true;
        }

        Player player = (Player) sender;

        switch (label.toLowerCase()) {
            case "fly":
                return utilManager.toggleFlight(player);

            case "speed":
                if (args.length < 1) {
                    player.sendMessage("Usage: /speed <value>");
                    return true;
                }
                return utilManager.setWalkSpeed(player, args[0]);

            case "flyspeed":
                if (args.length < 1) {
                    player.sendMessage("Usage: /flyspeed <value>");
                    return true;
                }
                return utilManager.setFlySpeed(player, args[0]);

            case "gamemode":
            case "gm":
                if (args.length < 1) {
                    player.sendMessage("Usage: /gamemode <survival|creative|spectator>");
                    return true;
                }
                return utilManager.setGameMode(player, args[0]);

            case "heal":
                return utilManager.healPlayer(player, args);

            case "feed":
                return utilManager.feedPlayer(player, args);

            case "rest":
                return utilManager.restPlayer(player, args);

            case "repair":
                return utilManager.repairItem(player);

            case "afk":
                return utilManager.toggleAFK(player);
            
            case "enderchest":
            case "ec":
                return utilManager.openEnderChest(player);
            
            case "trash":
                return utilManager.openTrash(player);
            
            case "top":
                return utilManager.teleportToTop(player);
            
            case "hat":
                return utilManager.wearHat(player);
            
            case "nick":
            case "nickname":
                return utilManager.setNickname(player, args, playerDataManager);
            
            case "realname":
                return utilManager.getRealName(player, args);
            
            case "list":
                return utilManager.listPlayers(player);
            
            case "near":
                return utilManager.nearPlayers(player, args);
            
            case "getpos":
                return utilManager.getPlayerPosition(player, args);
            
            case "ping":
                return utilManager.pingPlayer(player);
            
            case "seen":
                return utilManager.seenPlayer(player, args);
            
            case "workbench":
            case "wb":
            case "craft":
                return utilManager.openWorkbench(player);

            case "anvil":
                return utilManager.openAnvil(player);

            case "cartographytable":
                return utilManager.openCartographyTable(player);

            case "grindstone":
                return utilManager.openGrindstone(player);

            case "loom":
                return utilManager.openLoom(player);

            case "smithingtable":
            case "smithing":
                return utilManager.openSmithingTable(player);

            case "stonecutter":
                return utilManager.openStonecutter(player);

            case "ptime":
                return utilManager.setPlayerTime(player, args);

            case "pweather":
                return utilManager.setPlayerWeather(player, args);


            default:
                player.sendMessage("Unknown utility command.");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        List<String> suggestions = new ArrayList<>();
        switch (alias.toLowerCase()) {
            case "gamemode":
            case "gm":
                if (args.length == 1) {
                    suggestions.add("survival");
                    suggestions.add("creative");
                    suggestions.add("spectator");
                }
                break;

            case "speed":
            case "flyspeed":
                if (args.length == 1) {
                    suggestions.add("1");
                    suggestions.add("2");
                    suggestions.add("3");
                }
                break;
            
            case "near":
                if (args.length == 1) {
                    suggestions.add("<radius>");
                }
                break;
            
            case "nick":
            case "nickname":
                if (args.length == 1) {
                    suggestions.add("<new nickname>");
                }
                break;
            
            case "realname":
            case "seen":
            case "getpos":
                if (args.length == 1) {
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        suggestions.add(online.getName());
                    }
                }
                break;
            
            case "ptime":
                if (args.length == 1) {
                    suggestions.add("morning");
                    suggestions.add("noon");
                    suggestions.add("night");
                    suggestions.add("<ticks>");
                }
                break;
            
            case "pweather":
                if (args.length == 1) {
                    suggestions.add("clear");
                    suggestions.add("storm");
                    suggestions.add("thunder");
                }
                break;
            
            
        }
        return suggestions;
    }
}

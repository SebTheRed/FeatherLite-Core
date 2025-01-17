package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.PartyManager;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PartyCommands implements TabCompleter {

    private final PartyManager partyManager;

    public PartyCommands(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean handlePartyCommands(CommandSender sender, String[] args, boolean isPlayer, JavaPlugin plugin) {
        Player player = null;
        if (isPlayer) {
            player = (Player) sender;
        }
    
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /party <create|invite|accept|deny|leave|disband|list>");
            return true;
        }
    
        // Check permissions for general commands
        if (isPlayer && !(sender.hasPermission("core.party.player") || sender.hasPermission("core.party.admin") || sender.isOp())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command.");
            return true;
        }
    
        switch (args[0].toLowerCase()) {
            case "create":
                if (player == null) return true;
                partyManager.handleCreateParty(player);
                break;
            case "invite":
                if (player == null) return true;
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /party invite <player>");
                    return true;
                }
                partyManager.handleInvitePlayer(player, args[1]);
                break;
            case "accept":
                if (player == null) return true;
                partyManager.handleAcceptInvite(player);
                break;
            case "deny":
                if (player == null) return true;
                partyManager.handleDenyInvite(player);
                break;
            case "leave":
                if (player == null) return true;
                partyManager.handleLeaveParty(player);
                break;
            case "disband":
                // Admin or non-player disbanding another player's party
                if (!isPlayer) {
                    if (!(sender.hasPermission("core.party.admin") || sender.isOp())) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to disband other players' parties.");
                        return true;
                    }
                    if (args.length < 2) {
                        plugin.getLogger().warning("Usage: /party disband <player_name>");
                        return true;
                    }
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer == null) {
                        plugin.getLogger().warning("Player " + args[1] + " is not online or doesn't exist.");
                        return true;
                    }
                    if (partyManager.isInParty(targetPlayer)) {
                        partyManager.handleDisbandParty(targetPlayer);
                        plugin.getLogger().info("Admin has disbanded " + targetPlayer.getName() + "'s party.");
                        sender.sendMessage(ChatColor.GREEN + "You have disbanded " + targetPlayer.getName() + "'s party.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Player " + targetPlayer.getName() + " is not in a party.");
                    }
                    return true;
                }
    
                // Player disbanding their own party
                if (player != null) {
                    if (!(player.hasPermission("core.party.player") || sender.isOp())) {
                        player.sendMessage(ChatColor.RED + "You do not have permission to disband parties.");
                        return true;
                    }
    
                    if (!partyManager.isInParty(player)) {
                        player.sendMessage(ChatColor.RED + "You are not in a party.");
                        return true;
                    }
    
                    if (!partyManager.isPlayerPartyLeader(player)) {
                        player.sendMessage(ChatColor.RED + "Only the party leader can disband the party.");
                        return true;
                    }
    
                    partyManager.handleDisbandParty(player);
                    player.sendMessage(ChatColor.GREEN + "You have disbanded your party.");
                    return true;
                }
                break;
    
            case "list":
                if (player != null) {
                    partyManager.handleListPartyMembers(player);
                }
                break;
    
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /party <create|invite|accept|deny|leave|disband|list>");
        }
        return true;
    }

    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
    
        Player player = (Player) sender;
    
        // First argument: main subcommands
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            if (player.hasPermission("core.party.player") || player.hasPermission("core.party.admin") || player.isOp()) {
                suggestions.add("create");
                suggestions.add("invite");
                suggestions.add("accept");
                suggestions.add("deny");
                suggestions.add("leave");
                suggestions.add("list");
            }
            if (player.hasPermission("core.party.admin") || player.isOp()) {
                suggestions.add("disband");
            }
            return filterSuggestions(suggestions, args[0]);
        }
    
        // Second argument: players for "invite" or "disband"
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite") && (player.hasPermission("core.party.player") || player.isOp())) {
                List<String> onlinePlayers = new ArrayList<>();
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayers.add(onlinePlayer.getName());
                }
                return filterSuggestions(onlinePlayers, args[1]);
            }
    
            if (args[0].equalsIgnoreCase("disband") && (player.hasPermission("core.party.admin") || player.isOp())) {
                List<String> partyLeaders = partyManager.getAllPartyLeaders(); // Example method to fetch party leaders
                return filterSuggestions(partyLeaders, args[1]);
            }
        }
    
        return Collections.emptyList();
    }
    
    /**
     * Filters suggestions based on the current input.
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

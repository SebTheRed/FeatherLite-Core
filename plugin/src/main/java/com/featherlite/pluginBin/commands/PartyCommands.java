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
            player.sendMessage("Usage: /party <create|invite|accept|deny|leave|disband|list>");
            return true;
        }

        if (isPlayer && !(sender.hasPermission("core.party.player") || sender.isOp())) {
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
                    player.sendMessage("Usage: /party invite <player>");
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
                if (player == null) {
                    if (args.length < 2) { // If no player name is provided
                        plugin.getLogger().warning("You must add <player_name> to the end of /party disband <player_name>");
                        return true; // Stop execution to avoid errors
                    }
                    player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        plugin.getLogger().warning("Player " + args[1] + " is not online or doesn't exist.");
                        return true; // Stop execution
                    }
                } else {
                    partyManager.handleDisbandParty(player);
                }
                break;
            case "list":
                partyManager.handleListPartyMembers(player);
                break;
            default:
                player.sendMessage("Unknown command. Use /party <create|invite|accept|deny|leave|disband|list>");
        }
        return true;
    }

    // Tab completion logic
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "deny", "leave", "disband", "list");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")) {
            List<String> onlinePlayers = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                onlinePlayers.add(player.getName());
            }
            return onlinePlayers;
        }

        return Collections.emptyList();
    }
}

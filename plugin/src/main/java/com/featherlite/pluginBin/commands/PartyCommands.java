package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.lobbies.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PartyCommands implements TabCompleter {

    private final PartyManager partyManager;

    public PartyCommands(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    public boolean handlePartyCommands(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("Usage: /party <create|invite|accept|deny|leave|disband|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                partyManager.handleCreateParty(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage("Usage: /party invite <player>");
                    return true;
                }
                partyManager.handleInvitePlayer(player, args[1]);
                break;
            case "accept":
                partyManager.handleAcceptInvite(player);
                break;
            case "deny":
                partyManager.handleDenyInvite(player);
                break;
            case "leave":
                partyManager.handleLeaveParty(player);
                break;
            case "disband":
                partyManager.handleDisbandParty(player);
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

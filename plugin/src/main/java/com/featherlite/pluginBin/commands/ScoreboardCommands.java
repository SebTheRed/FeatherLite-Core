package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.scoreboards.ScoreboardManager;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardCommands implements TabCompleter {
    private final ScoreboardManager scoreboardManager;

    public ScoreboardCommands(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public boolean handleScoreboardCommands(CommandSender sender, String[] args, boolean isPlayer) {
        Player player = (isPlayer ? (Player) sender : null);

        if (args.length == 0) {
            sender.sendMessage("Usage: /scoreboard <toggle|[name]>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle":
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "This command (/board toggle) can only be typed by players."); return true;}
                scoreboardManager.toggleScoreboard(player);
                return true;
            case "reload":
                if (player != null && !player.hasPermission("core.board.reload")) {
                    sender.sendMessage("You don't have permission to reload scoreboards.");
                    return true;
                }
                scoreboardManager.reloadScoreboards();
                sender.sendMessage("Scoreboards reloaded successfully.");
                return true;
            case "list":
                List<String> activeScoreboards = scoreboardManager.getActiveScoreboards();
                if (activeScoreboards.isEmpty()) {
                    sender.sendMessage("No active scoreboards at the moment.");
                } else {
                    sender.sendMessage("Active Scoreboards:");
                    for (String scoreboard : activeScoreboards) {
                        sender.sendMessage(" - " + scoreboard);
                    }
                }
                return true;
            default:
                if (!isPlayer) {sender.sendMessage(ChatColor.RED + "This command (/board <name>) can only be typed by players."); return true;}
                if (isPlayer && !sender.hasPermission("core.board.switch")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to manually switch your scoreboard.");
                    return true;
                }
                String scoreboardName = args[0];
                if (scoreboardManager.renderScoreboard(player, scoreboardName)) {
                    sender.sendMessage("Switched to scoreboard: " + scoreboardName);
                } else {
                    sender.sendMessage("Scoreboard not found: " + scoreboardName);
                }
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        if (args.length == 1) {
            // First argument: suggest "toggle" or available scoreboard names
            List<String> suggestions = new ArrayList<>();
            suggestions.add("toggle");
            suggestions.addAll(scoreboardManager.getAvailableScoreboards()); // Fetch registered scoreboard names
            return suggestions;
        }

        return Collections.emptyList();
    }
}

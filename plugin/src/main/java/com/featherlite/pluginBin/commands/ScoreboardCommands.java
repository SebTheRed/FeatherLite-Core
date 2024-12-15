package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.scoreboards.ScoreboardManager;
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

    public boolean handleScoreboardCommands(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use scoreboard commands.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Usage: /scoreboard <toggle|[name]>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "toggle":
                scoreboardManager.toggleScoreboard(player);
                return true;
            case "reload":
                if (!player.hasPermission("core.board.reload"))
                {
                    player.sendMessage("You don't have permission to reload scoreboards.");
                    return true;
                }
                scoreboardManager.reloadScoreboards();
                player.sendMessage("Scoreboards reloaded successfully.");
                return true;
            case "list":
                List<String> activeScoreboards = scoreboardManager.getActiveScoreboards();
                if (activeScoreboards.isEmpty()) {
                    player.sendMessage("No active scoreboards at the moment.");
                } else {
                    player.sendMessage("Active Scoreboards:");
                    for (String scoreboard : activeScoreboards) {
                        player.sendMessage(" - " + scoreboard);
                    }
                }
                return true;
            default:
                String scoreboardName = args[0];
                if (scoreboardManager.renderScoreboard(player, scoreboardName)) {
                    player.sendMessage("Switched to scoreboard: " + scoreboardName);
                } else {
                    player.sendMessage("Scoreboard not found: " + scoreboardName);
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

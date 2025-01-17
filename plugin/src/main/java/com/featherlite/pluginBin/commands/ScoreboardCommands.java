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
                if (player != null && !(player.hasPermission("core.board.reload") || sender.isOp())) {
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
                if (isPlayer && !(sender.hasPermission("core.board.switch") || sender.isOp())) {
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
    
        Player player = (Player) sender;
    
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
    
            // Add "toggle" for players with the toggle permission or OP
            if (player.hasPermission("core.board.toggle") || player.isOp()) {
                suggestions.add("toggle");
            }
    
            // Add "reload" for players with the reload permission or OP
            if (player.hasPermission("core.board.reload") || player.isOp()) {
                suggestions.add("reload");
            }
    
            // Add "list" for everyone, as listing doesn't seem to require special permissions
            suggestions.add("list");
    
            // Add scoreboard names for players with the switch permission or OP
            if (player.hasPermission("core.board.switch") || player.isOp()) {
                suggestions.addAll(scoreboardManager.getAvailableScoreboards());
            }
    
            return filterSuggestions(suggestions, args[0]);
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

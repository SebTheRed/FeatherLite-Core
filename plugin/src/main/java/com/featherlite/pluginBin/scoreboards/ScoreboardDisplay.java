package com.featherlite.pluginBin.scoreboards;

import com.featherlite.pluginBin.placeholders.PlaceholderManager;
import com.featherlite.pluginBin.utils.ColorUtils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardDisplay {
    private final String name;
    private final String title;
    private final List<String> lines;
    private final PlaceholderManager placeholderManager;

    public ScoreboardDisplay(String name, FileConfiguration config, Plugin plugin, boolean isDebuggerOn) {
        this.name = name;
        this.title = config.getString("scoreboard.title", "Scoreboard");
        this.lines = config.getStringList("scoreboard.lines");
        this.placeholderManager = PlaceholderManager.getInstance(plugin);
    }

    public void start(Player player) {
        // Create a new scoreboard
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        // Register an objective using modern methods
        Objective objective = scoreboard.registerNewObjective(
                name, // Unique name of the objective
                Criteria.DUMMY, // Use the modern Criteria enum
                ColorUtils.parseColors(placeholderManager.resolvePlaceholders(title, player)), // Title with colors
                RenderType.INTEGER // Use integer render type for scores
        );

        objective.setDisplaySlot(DisplaySlot.SIDEBAR); // Display the scoreboard on the sidebar

        // Add lines to the scoreboard
        int scoreValue = lines.size();
        for (String line : lines) {
            String resolvedLine = placeholderManager.resolvePlaceholders(line, player);
            objective.getScore(ColorUtils.parseColors(resolvedLine)).setScore(scoreValue--); // Add translated line
        }

        // Set the scoreboard for the player
        player.setScoreboard(scoreboard);
    }

    public void stop(Player player) {
        // Reset the player's scoreboard to an empty one
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void update(Player player) {
        Scoreboard scoreboard = player.getScoreboard(); // Get the player's current scoreboard
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR); // Sidebar objective
    
        if (objective == null) {
            return; // No sidebar objective; nothing to update
        }
    
        // Update title
        objective.setDisplayName(ColorUtils.parseColors(placeholderManager.resolvePlaceholders(title, player)));
    
        // Clear old lines
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry); // Remove all existing lines
        }
    
        // Add updated lines
        int scoreValue = lines.size();
        for (String line : lines) {
            String resolvedLine = placeholderManager.resolvePlaceholders(line, player);
            objective.getScore(ColorUtils.parseColors(resolvedLine)).setScore(scoreValue--); // Set the new score
        }
    }
    
    

    public String getName() {
        return name;
    }

}

package com.featherlite.pluginBin.scoreboards;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreboardManager implements Listener {
    private final Plugin plugin;
    private final Map<String, ScoreboardDisplay> scoreboards = new HashMap<>();
    private final Map<Player, ScoreboardDisplay> activeScoreboards = new HashMap<>();
    private final boolean isDebuggerOn;

    public ScoreboardManager(Plugin plugin, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.isDebuggerOn = isDebuggerOn;
    
        // Ensure the scoreboards folder exists
        File scoreboardFolder = new File(plugin.getDataFolder(), "scoreboards");
        if (!scoreboardFolder.exists() && scoreboardFolder.mkdirs()) {
            plugin.getLogger().info("Created scoreboards folder.");
        }
    
        // Ensure the default-board.yml exists
        ensureDefaultBoard(scoreboardFolder);
    
        // Load all scoreboards
        loadScoreboardsFromAllPlugins();
    
        // Register this class as a listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    
        // Start the scoreboard update task
        startUpdateTask();
    }
    

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    
        // Display the default scoreboard to the player
        boolean rendered = renderScoreboard(player, "default-board");
        if (!rendered) {
            player.sendMessage("Default scoreboard not found. Please check your configuration.");
        }
    }

    public void reloadScoreboards() {
        // Reload all scoreboards
        loadScoreboardsFromAllPlugins();
        plugin.getLogger().info("All scoreboards reloaded.");
    
        // Re-render scoreboards for all players with active scoreboards
        for (Map.Entry<Player, ScoreboardDisplay> entry : activeScoreboards.entrySet()) {
            Player player = entry.getKey();
            ScoreboardDisplay display = entry.getValue();
    
            // Stop the current scoreboard
            display.stop(player);
    
            // Re-render the scoreboard
            String scoreboardName = display.getName(); // Assuming getName() exists in ScoreboardDisplay
            if (!renderScoreboard(player, scoreboardName)) {
                plugin.getLogger().warning("Failed to re-render scoreboard: " + scoreboardName + " for player: " + player.getName());
            } else {
                plugin.getLogger().info("Successfully re-rendered scoreboard: " + scoreboardName + " for player: " + player.getName());
            }
        }
    }

    // Load scoreboards from all plugins
    public void loadScoreboardsFromAllPlugins() {
        scoreboards.clear();

        // Load core scoreboards first
        loadScoreboardsFromPlugin(plugin, isDebuggerOn);

        // Load scoreboards from all other plugins
        for (Plugin installedPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (installedPlugin.isEnabled() && !installedPlugin.equals(plugin)) {
                loadScoreboardsFromPlugin(installedPlugin, isDebuggerOn);
            }
        }
    }

    private void ensureDefaultBoard(File scoreboardFolder) {
        File defaultBoardFile = new File(scoreboardFolder, "default-board.yml");
        if (!defaultBoardFile.exists()) {
            plugin.getLogger().info("Default scoreboard not found. Copying default-board.yml from resources...");
            try {
                // Load resource from plugin jar
                plugin.saveResource("scoreboards/default-board.yml", false); // Copy to data folder
                plugin.getLogger().info("Successfully copied default-board.yml.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to copy default-board.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<String> getActiveScoreboards() {
        List<String> active = new ArrayList<>();
        for (Map.Entry<Player, ScoreboardDisplay> entry : activeScoreboards.entrySet()) {
            active.add(entry.getValue().getName()); // Add scoreboard name
            plugin.getLogger().info("Active scoreboard for " + entry.getKey().getName() + ": " + entry.getValue().getName());
        }
        return active;
    }

    // Load scoreboards from a single plugin
    private void loadScoreboardsFromPlugin(Plugin plugin, boolean isDebuggerOn) {
        plugin.getLogger().info("Loading scoreboards from " + plugin.getName());
        ScoreboardParser parser = new ScoreboardParser(plugin, isDebuggerOn);
        Map<String, ScoreboardDisplay> pluginScoreboards = parser.parseScoreboards();

        // Register all scoreboards from this plugin
        scoreboards.putAll(pluginScoreboards);
    }

    // Render a scoreboard for a player
    public boolean renderScoreboard(Player player, String scoreboardName) {
        ScoreboardDisplay display = scoreboards.get(scoreboardName);
        if (display == null) return false;
    
        // Check for permissions UPDATE PERMISSIONS LATER
        // String permission = display.getPermission(); // Add a getter in ScoreboardDisplay
        // if (permission != null && !player.hasPermission(permission)) {
        //     player.sendMessage("You don't have permission to view this scoreboard.");
        //     return false;
        // }
    
        // Render the scoreboard
        if (activeScoreboards.containsKey(player)) {
            activeScoreboards.get(player).stop(player);
        }
        activeScoreboards.put(player, display);
        display.start(player);
        return true;
    }

    public void setPlayerScoreboard(Player player, String scoreboardName) {
        // Get the requested scoreboard
        ScoreboardDisplay display = scoreboards.get(scoreboardName);
        if (display == null) {
            player.sendMessage("Scoreboard not found: " + scoreboardName);
            return;
        }
    
        // Stop the current scoreboard for the player (if any)
        if (activeScoreboards.containsKey(player)) {
            activeScoreboards.get(player).stop(player);
        }
    
        // Start the new scoreboard
        activeScoreboards.put(player, display);
        display.start(player);
    
        player.sendMessage("Your scoreboard has been updated to: " + scoreboardName);
    }


    // Toggle a player's scoreboard
    public void toggleScoreboard(Player player) {
        if (activeScoreboards.containsKey(player)) {
            // Stop and remove the current scoreboard
            activeScoreboards.get(player).stop(player);
            activeScoreboards.remove(player);
            if (isDebuggerOn) {player.sendMessage("Scoreboard hidden.");}
        } else {
            // Render the default scoreboard
            if (!renderScoreboard(player, "default-board")) {
                player.sendMessage("Default scoreboard not found. Please check your configuration.");
            } else {
                player.sendMessage("Default scoreboard displayed.");
            }
        }
    }



    public void startUpdateTask() {
        int updateInterval = 5; // Default interval (in seconds)
        
        // Use the update-interval from the default-board.yml, if available
        File defaultBoardFile = new File(plugin.getDataFolder(), "scoreboards/default-board.yml");
        if (defaultBoardFile.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(defaultBoardFile);
            updateInterval = config.getInt("scoreboard.update-interval", 5); // Default to 5 seconds if not set
        }
    
        int finalUpdateInterval = updateInterval;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Map.Entry<Player, ScoreboardDisplay> entry : activeScoreboards.entrySet()) {
                Player player = entry.getKey();
                ScoreboardDisplay display = entry.getValue();
    
                // Dynamically update the scoreboard
                display.update(player);
            }
        }, 0L, finalUpdateInterval * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    }
    







    // Update all active scoreboards
    public void updateScoreboards() {
        for (Map.Entry<Player, ScoreboardDisplay> entry : activeScoreboards.entrySet()) {
            entry.getValue().update(entry.getKey());
        }
    }

    public List<String> getAvailableScoreboards() {
        return new ArrayList<>(scoreboards.keySet());
    }




}

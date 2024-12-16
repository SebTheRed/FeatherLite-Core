package com.featherlite.pluginBin.stats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatsManager implements Listener {
    private final File statsFolder;
    private final Map<UUID, Map<String, Object>> inMemoryStats = new HashMap<>();
    private final Plugin plugin;

    public PlayerStatsManager(Plugin plugin) {
        this.plugin = plugin;
        this.statsFolder = new File(plugin.getDataFolder(), "player_stats");
        if (!statsFolder.exists()) {
            statsFolder.mkdirs();
        }

        // Schedule periodic save task
        startSaveTask();
    }

    /**
     * Load player stats into memory when they join.
     */
    public void loadPlayerStats(Player player) {
        FileConfiguration statsConfig = getPlayerStatsFile(player);
        Map<String, Object> stats = new HashMap<>();

        // Load stats into the in-memory map
        statsConfig.getKeys(true).forEach(key -> stats.put(key, statsConfig.get(key)));
        inMemoryStats.put(player.getUniqueId(), stats);
    }

    /**
     * Save player stats from memory to file.
     */
    public void savePlayerStats(Player player) {
        UUID playerId = player.getUniqueId();
        FileConfiguration statsConfig = getPlayerStatsFile(player);

        // Save in-memory stats to the YAML file
        Map<String, Object> stats = inMemoryStats.get(playerId);
        if (stats != null) {
            stats.forEach(statsConfig::set);

            try {
                statsConfig.save(getStatsFile(player));
            } catch (IOException e) {
                System.err.println("Failed to save stats for player: " + player.getName() + " (" + playerId + ")");
                e.printStackTrace();
            }
        }
    }

    /**
     * Update an in-memory stat for a player.
     */
    public void updateStat(Player player, String key, Object value) {
        inMemoryStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, value);
    }

    /**
     * Get a stat for a player.
     */
    public Object getStat(Player player, String key) {
        return inMemoryStats.getOrDefault(player.getUniqueId(), new HashMap<>()).get(key);
    }

    /**
     * Periodically save all stats from memory to file.
     */
    private void startSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                inMemoryStats.forEach((uuid, stats) -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        savePlayerStats(player);
                    }
                });
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Runs every 5 minutes (6000 ticks)
    }

    /**
     * Handle player quit: save their stats to disk.
     */
    public void onPlayerQuit(Player player) {
        savePlayerStats(player);
        inMemoryStats.remove(player.getUniqueId());
    }

    /**
     * Get the FileConfiguration for a player's stats file.
     */
    private FileConfiguration getPlayerStatsFile(Player player) {
        return YamlConfiguration.loadConfiguration(getStatsFile(player));
    }

    /**
     * Get the stats file for a player.
     */
    private File getStatsFile(Player player) {
        return new File(statsFolder, player.getName() + "-" + player.getUniqueId() + ".yml");
    }
}

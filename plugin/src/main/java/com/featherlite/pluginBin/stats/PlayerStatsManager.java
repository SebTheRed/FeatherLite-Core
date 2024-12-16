package com.featherlite.pluginBin.stats;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerStatsManager implements Listener {
    private final File statsFolder;
    private final Map<UUID, Map<String, Object>> inMemoryStats = new HashMap<>();
    private final Plugin plugin;

    // Predefined list of tracked stats
    private static final List<Statistic> trackedStats = List.of(
            Statistic.WALK_ONE_CM, Statistic.SWIM_ONE_CM, Statistic.FLY_ONE_CM,
            Statistic.SPRINT_ONE_CM, Statistic.MOB_KILLS, Statistic.PLAYER_KILLS,
            Statistic.DEATHS, Statistic.DAMAGE_DEALT, Statistic.DAMAGE_TAKEN,
            Statistic.JUMP, Statistic.CRAFT_ITEM, Statistic.BREAK_ITEM
    );

    public PlayerStatsManager(Plugin plugin) {
        this.plugin = plugin;
        this.statsFolder = new File(plugin.getDataFolder(), "player_stats");
        if (!statsFolder.exists()) {
            statsFolder.mkdirs();
        }

        // Start periodic task
        startSaveTask();
    }

    /**
     * Load stats into memory when a player joins.
     */
    public void loadPlayerStats(Player player) {
        FileConfiguration statsConfig = getPlayerStatsFile(player);
        Map<String, Object> stats = new HashMap<>();

        // Load stats into memory
        statsConfig.getKeys(true).forEach(key -> stats.put(key, statsConfig.get(key)));
        inMemoryStats.put(player.getUniqueId(), stats);
    }

    /**
     * Save stats from memory to file for a specific player.
     */
    public void savePlayerStats(Player player) {
        UUID playerId = player.getUniqueId();
        FileConfiguration statsConfig = getPlayerStatsFile(player);

        Map<String, Object> stats = inMemoryStats.get(playerId);
        if (stats != null) {
            stats.forEach(statsConfig::set);

            try {
                statsConfig.save(getStatsFile(player));
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save stats for player: " + player.getName());
                e.printStackTrace();
            }
        }
    }

    /**
     * Update a specific stat in memory.
     */
    public void updateStat(Player player, String key, Object value) {
        inMemoryStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, value);
    }

    /**
     * Increment a player's stat by 1. If the stat doesn't exist, initialize it to 1.
     *
     * @param player The player whose stat needs to be incremented
     * @param key    The key of the stat to increment
     * Example usage:     statsManager.incrementStat(player, "bedwars.beds-broken");
     */
    public void incrementStat(Player player, String key, int amount) {
        // Retrieve the current value or default to 0
        Map<String, Object> playerStats = inMemoryStats.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        int currentValue = (int) playerStats.getOrDefault(key, 0);

        // Increment the value
        playerStats.put(key, currentValue + amount);

        // plugin.getLogger().info("Stat incremented: " + key + " -> " + (currentValue + amount));   // ADD THIS WITH DEBUGGER
    }

    /**
     * Get a specific stat value.
     */
    public Object getStat(Player player, String key) {
        return inMemoryStats
                .getOrDefault(player.getUniqueId(), new HashMap<>())
                .getOrDefault(key, 0);
    }

    /**
     * Periodic task to poll all tracked stats and save them.
     */
    private void startSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    UUID playerId = player.getUniqueId();
                    Map<String, Object> stats = inMemoryStats.computeIfAbsent(playerId, k -> new HashMap<>());
    
                    // Poll and save untyped stats only
                    for (Statistic stat : trackedStats) {
                        try {
                            // Skip stats that require additional parameters
                            if (stat.getType() == Statistic.Type.UNTYPED) {
                                String yamlKey = "stats.general." + stat.name().toLowerCase().replace("_", "-");
                                int statValue = player.getStatistic(stat);
                                stats.put(yamlKey, statValue);
                            }
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Skipped parameterized statistic: " + stat.name());
                        }
                    }
    
                    savePlayerStats(player);
                });
                // plugin.getLogger().info("Polled and saved stats for all online players."); // REACTIVATE W/ DEBUGGER
            }
        }.runTaskTimer(plugin, 1200L, 1200L); // Every 60 seconds
    }
    

    /**
     * Handle player quit: save stats and clean up.
     */
    public void onPlayerQuit(Player player) {
        savePlayerStats(player);
        inMemoryStats.remove(player.getUniqueId());
    }

    /**
     * Get the stats file configuration for a player.
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

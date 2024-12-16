package com.featherlite.pluginBin.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatListeners implements Listener {
    private final PlayerStatsManager statsManager;

    public StatListeners(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    /**
     * Load player stats when they join.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        statsManager.loadPlayerStats(player);
    }

    /**
     * Save and clean up stats when they quit.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        statsManager.onPlayerQuit(player);
    }

    /**
     * Custom handler: track block placements.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String yamlKey = "stats.general.blocks-placed";

        int currentValue = (int) statsManager.getStat(player, yamlKey);
        statsManager.updateStat(player, yamlKey, currentValue + 1);
    }
}

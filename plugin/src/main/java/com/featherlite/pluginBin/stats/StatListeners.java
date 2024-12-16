package com.featherlite.pluginBin.stats;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;

public class StatListeners implements Listener {
    private final PlayerStatsManager statsManager;

    public StatListeners(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    /**
     * Handle player join: Load stats into memory.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        statsManager.loadPlayerStats(player);
    }

    /**
     * Handle player quit: Save stats and clean up memory.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        statsManager.onPlayerQuit(player);
    }

    /**
     * Handle player block place.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String yamlKey = "stats.general.blocks-placed";

        int currentValue = (int) statsManager.getStat(player, yamlKey);
        statsManager.updateStat(player, yamlKey, currentValue + 1);
    }

    /**
     * Track distances and other statistics.
     */
    @EventHandler
public void onPlayerStatisticIncrement(PlayerStatisticIncrementEvent event) {
    Player player = event.getPlayer();
    Statistic stat = event.getStatistic();

    // Map Statistic to YAML key
    String yamlKey = switch (stat) {
        case ANIMALS_BRED -> "stats.general.animals-bred";
        case ARMOR_CLEANED -> "stats.general.armor-cleaned";
        case AVIATE_ONE_CM -> "stats.general.distance-aviate";
        case BELL_RING -> "stats.general.bell-rung";
        case BOAT_ONE_CM -> "stats.general.distance-boated";
        case BREAK_ITEM -> "stats.general.items-broken";
        case CLIMB_ONE_CM -> "stats.general.distance-climbed";
        case CRAFT_ITEM -> "stats.general.items-crafted";
        case CROUCH_ONE_CM -> "stats.general.distance-crouched";
        case DAMAGE_DEALT -> "stats.general.damage-dealt";
        case DAMAGE_TAKEN -> "stats.general.damage-taken";
        case DEATHS -> "stats.general.deaths-total";
        case FALL_ONE_CM -> "stats.general.distance-fallen";
        case FISH_CAUGHT -> "stats.general.fishing-catches";
        case FLOWER_POTTED -> "stats.general.flower-potted";
        case FLY_ONE_CM -> "stats.general.distance-flown";
        case HORSE_ONE_CM -> "stats.general.distance-horse";
        case ITEM_ENCHANTED -> "stats.general.enchantments-made";
        case JUMP -> "stats.general.jumps";
        case MINE_BLOCK -> "stats.general.blocks-mined";
        case MINECART_ONE_CM -> "stats.general.distance-minecart";
        case MOB_KILLS -> "stats.general.kills-mobs";
        case PIG_ONE_CM -> "stats.general.distance-pig";
        case PLAY_ONE_MINUTE -> "stats.general.time-ticks";
        case PLAYER_KILLS -> "stats.general.kills-players";
        case RAID_WIN -> "stats.general.raid-won";
        case RECORD_PLAYED -> "stats.general.records-played";
        case SLEEP_IN_BED -> "stats.general.beds-slept-in";
        case SPRINT_ONE_CM -> "stats.general.distance-sprinted";
        case STRIDER_ONE_CM -> "stats.general.distance-strider";
        case SWIM_ONE_CM -> "stats.general.distance-swam";
        case TRADED_WITH_VILLAGER -> "stats.general.trades-completed";
        case WALK_ONE_CM -> "stats.general.distance-walked";
        case WALK_UNDER_WATER_ONE_CM -> "stats.general.distance-walked-underwater";
        default -> null;
    };

    // If the stat is supported, update it in the stats manager
    if (yamlKey != null) {
        int currentValue = (int) statsManager.getStat(player, yamlKey);
        statsManager.updateStat(player, yamlKey, currentValue + event.getNewValue());
    }
}
}

package com.featherlite.pluginBin.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlowUtil {
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitRunnable> activeGlows = new HashMap<>();
    private final Scoreboard scoreboard;

    public GlowUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * Applies a glow effect to an entity with a specific color.
     *
     * @param entity The entity to apply the glow effect to.
     * @param color  The color for the glow effect.
     * @param durationInSeconds Duration of the glow effect in seconds, or -1 for infinite.
     */
    public void applyGlow(Entity entity, ChatColor color, int durationInSeconds) {
        if (entity == null || !entity.isValid()) return;

        // Enable glowing and add the entity to a team to apply the glow color
        entity.setGlowing(true);
        addEntityToTeamWithColor(entity, color);

        // Schedule removal of glow after duration if specified
        if (durationInSeconds > 0) {
            UUID entityId = entity.getUniqueId();
            cancelExistingGlowTask(entityId);

            // Task to remove glow after the specified duration
            BukkitRunnable glowTask = new BukkitRunnable() {
                @Override
                public void run() {
                    removeGlow(entity);
                }
            };
            glowTask.runTaskLater(plugin, durationInSeconds * 20L); // Convert seconds to ticks
            activeGlows.put(entityId, glowTask);
        }
    }

    /**
     * Removes the glow effect from an entity.
     *
     * @param entity The entity to remove the glow effect from.
     */
    public void removeGlow(Entity entity) {
        if (entity == null || !entity.isValid()) return;

        // Disable glowing and remove the entity from any glow team
        entity.setGlowing(false);
        removeEntityFromGlowTeam(entity);

        // Cancel any scheduled tasks for this entity
        cancelExistingGlowTask(entity.getUniqueId());
    }

    /**
     * Adds an entity to a team with the specified glow color.
     *
     * @param entity The entity to add to the team.
     * @param color  The color for the glow effect.
     */
    private void addEntityToTeamWithColor(Entity entity, ChatColor color) {
        String teamName = "glow_" + color.name();

        // Get or create the team for the specified color
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
            team.setColor(color);
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER); // Hide name tag if desired
        }

        // Add entity to the team
        if (entity instanceof Player) {
            team.addEntry(((Player) entity).getName());
        } else {
            team.addEntry(entity.getUniqueId().toString());
        }
    }

    /**
     * Removes an entity from any glow team.
     *
     * @param entity The entity to remove from the team.
     */
    private void removeEntityFromGlowTeam(Entity entity) {
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("glow_")) {
                if (entity instanceof Player && team.hasEntry(((Player) entity).getName())) {
                    team.removeEntry(((Player) entity).getName());
                } else if (team.hasEntry(entity.getUniqueId().toString())) {
                    team.removeEntry(entity.getUniqueId().toString());
                }
            }
        }
    }

    /**
     * Cancels any existing glow removal task for a given entity.
     *
     * @param entityId The UUID of the entity.
     */
    private void cancelExistingGlowTask(UUID entityId) {
        BukkitRunnable existingTask = activeGlows.remove(entityId);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }
    }

    /**
     * Removes all active glows from entities and cancels all scheduled tasks.
     */
    public void clearAllGlows() {
        for (UUID entityId : activeGlows.keySet()) {
            Entity entity = Bukkit.getEntity(entityId);
            if (entity != null && entity.isValid()) {
                entity.setGlowing(false);
                removeEntityFromGlowTeam(entity);
            }
        }
        activeGlows.values().forEach(BukkitRunnable::cancel);
        activeGlows.clear();
    }
}

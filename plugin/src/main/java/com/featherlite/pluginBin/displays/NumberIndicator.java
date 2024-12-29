package com.featherlite.pluginBin.displays;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class NumberIndicator {

    private final JavaPlugin plugin;
    private final DisplayPieceManager displayPieceManager;

    public NumberIndicator(JavaPlugin plugin, DisplayPieceManager displayPieceManager) {
        this.plugin = plugin;
        this.displayPieceManager = displayPieceManager;
    }

    /**
     * Spawns a damage indicator at the specified location.
     *
     * @param location The location to spawn the indicator.
     * @param damage   The amount of damage to display.
     */
    public void spawnDamageIndicator(Location location, double damage) {
        if (!plugin.getConfig().getBoolean("show-damage-indicators", true)) {
            return; // Check if damage indicators are enabled in the config
        }

        String text = "§c-" + String.format("%.1f", damage) + "♡"; // Red text for damage
        spawnIndicator(location, text, "damage");
    }

    /**
     * Spawns a healing indicator at the specified location.
     *
     * @param location The location to spawn the indicator.
     * @param healing  The amount of healing to display.
     */
    public void spawnHealingIndicator(Location location, double healing) {
        if (!plugin.getConfig().getBoolean("show-healing-indicators", true)) {
            return; // Check if healing indicators are enabled in the config
        }

        String text = "§a+" + String.format("%.1f", healing) + "♡"; // Green text for healing
        spawnIndicator(location, text, "healing");
    }

    /**
     * Generic method to spawn a floating indicator with custom text.
     *
     * @param location The location to spawn the indicator.
     * @param text     The text to display.
     * @param idPrefix The prefix for the display piece ID.
     */
    public void spawnIndicator(Location location, String text, String idPrefix) {
        World world = location.getWorld();
        if (world == null) return;

        // Generate a unique ID for the display piece
        String id = idPrefix + "_" + System.currentTimeMillis();

        // Use DisplayPieceManager to create a TextDisplay
        DisplayPiece displayPiece = displayPieceManager.createTextDisplay(id, world, location, text, false);

        // Customize properties if needed
        displayPiece.setBillboard(org.bukkit.entity.Display.Billboard.CENTER); // Always face the player
        displayPiece.setBrightness(15, 15); // Max brightness
        displayPiece.setShadowStrength(0.5f);

        // Animate upward movement over 15 ticks (0.75 seconds)
        Location targetLocation = location.clone().add(0, 1.5, 0); // Move 1.5 blocks upward
        displayPiece.move(targetLocation, 20); // Smooth translation

        // Schedule automatic removal of the display after 15 ticks
        Bukkit.getScheduler().runTaskLater(plugin, () -> displayPieceManager.removeDisplay(id), 20L);
    }
}


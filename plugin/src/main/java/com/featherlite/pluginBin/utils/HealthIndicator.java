package com.featherlite.pluginBin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;

public class HealthIndicator {

    private final JavaPlugin plugin;

    public HealthIndicator(JavaPlugin plugin) {
        this.plugin = plugin;
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

        spawnIndicator(location, "§c-" + String.format("%.1f", damage)); // Red text for damage
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

        spawnIndicator(location, "§a+" + String.format("%.1f", healing)); // Green text for healing
    }

    /**
     * Generic method to spawn a floating indicator with custom text.
     *
     * @param location The location to spawn the indicator.
     * @param text     The text to display.
     */
    private void spawnIndicator(Location location, String text) {
        World world = location.getWorld();
        if (world == null) return;

        // Spawn a TextDisplay entity
        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);

        // Set up the TextDisplay properties
        textDisplay.setBillboard(Billboard.CENTER); // Makes the text face the player
        textDisplay.setText(text); // Set the display text
        textDisplay.setDefaultBackground(false); // Removes the default background
        textDisplay.setCustomNameVisible(false); // No nameplate needed
        textDisplay.setBrightness(new TextDisplay.Brightness(15, 15)); // Maximum light level
        textDisplay.setShadowStrength(0.5F); // Adds a slight shadow for visibility
        textDisplay.setViewRange(50.0F); // Adjust view range to suit your needs

        // Schedule removal of the TextDisplay after 15 ticks (0.75 seconds)
        Bukkit.getScheduler().runTaskLater(plugin, textDisplay::remove, 15L);
    }
}

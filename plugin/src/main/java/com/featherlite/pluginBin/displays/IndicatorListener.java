package com.featherlite.pluginBin.displays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.java.JavaPlugin;



public class IndicatorListener implements Listener {

    private final JavaPlugin plugin;
    private final NumberIndicator healthIndicator;

    public IndicatorListener(JavaPlugin plugin, DisplayPieceManager displayPieceManager) {
        this.plugin = plugin;
        this.healthIndicator = new NumberIndicator(plugin, displayPieceManager);
        Bukkit.getPluginManager().registerEvents(this, plugin); // Register the listener
    }

    /**
     * Handles entity damage events to spawn damage indicators.
     *
     * @param event The EntityDamageByEntityEvent.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // if (!(event.getEntity() instanceof Player)) return; // Only show indicators for players

        // Get the location of the hit entity
        Location location = event.getEntity().getLocation().add(0, 1, 0); // Position slightly above the entity

        // Get the final damage dealt after any adjustments
        double finalDamage = event.getFinalDamage();

        // Spawn the damage indicator
        healthIndicator.spawnDamageIndicator(location, finalDamage);
    }

    /**
     * Handles entity healing events to spawn healing indicators.
     *
     * @param event The EntityRegainHealthEvent.
     */
    @EventHandler
    public void onEntityHeal(EntityRegainHealthEvent event) {
        // if (!(event.getEntity() instanceof Player)) return; // Only show indicators for players

        // Get the location of the healed entity
        Location location = event.getEntity().getLocation().add(0, 1, 0); // Position slightly above the entity

        // Get the amount of health regained
        double healAmount = event.getAmount();

        // Spawn the healing indicator
        healthIndicator.spawnHealingIndicator(location, healAmount);
    }
}

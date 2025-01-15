// SpectatorUtils.java
package com.featherlite.pluginBin.utils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpectatorUtils implements Listener {
    private final Set<UUID> spectators = new HashSet<>(); // Track players in "spectator mode"

    public SpectatorUtils(JavaPlugin plugin) {
        // Register event listener for damage handling
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void enableSpectatorMode(Player player) {
        // Set to Survival Mode, enable flight, and apply invisibility effect
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true); // Set the player to be flying initially
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
        
        // Add player to the spectator set and make them invincible
        spectators.add(player.getUniqueId());
        player.sendMessage("You are now in feather-spectator mode.");
    }

    public void disableSpectatorMode(Player player) {
        // Remove invisibility effect, disable flight, and take player out of spectator set
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setAllowFlight(false);
        player.setFlying(false);
        spectators.remove(player.getUniqueId());
        
        player.setGameMode(GameMode.SURVIVAL);
        player.sendMessage("You have left feather-spectator mode.");
    }

    public boolean isInSpectatorMode(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Cancel damage events for players in spectator mode
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (spectators.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}

package com.featherlite.pluginBin.essentials.teleportation;

import com.featherlite.pluginBin.essentials.teleportation.TeleportationManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerRespawnListener implements Listener {
    private final TeleportationManager teleportationManager;
    private final JavaPlugin plugin;

    public PlayerRespawnListener(TeleportationManager teleportationManager, JavaPlugin plugin) {
        this.teleportationManager = teleportationManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location spawnLocation = teleportationManager.getServerSpawn();

        if (spawnLocation != null) {
            event.setRespawnLocation(spawnLocation);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawnLocation);
            }, 1L); // Delay 1 tick to ensure proper teleportation
        }
    }
}

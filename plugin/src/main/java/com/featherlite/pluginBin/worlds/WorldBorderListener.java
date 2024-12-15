package com.featherlite.pluginBin.worlds;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderListener implements Listener {
    private final WorldManager worldManager;

    public WorldBorderListener(WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) return;
    
        String worldName = to.getWorld().getName();
        Double borderRadius = worldManager.getWorldBorder(worldName);
        if (borderRadius == null) return; // No border for this world
    
        // Calculate square border limits
        double minX = -borderRadius;
        double maxX = borderRadius;
        double minZ = -borderRadius;
        double maxZ = borderRadius;
    
        // Check if the player is outside the border
        if (to.getX() < minX || to.getX() > maxX || to.getZ() < minZ || to.getZ() > maxZ) {
            // Teleport the player back to the edge of the border
            double safeX = Math.max(minX, Math.min(maxX, to.getX()));
            double safeZ = Math.max(minZ, Math.min(maxZ, to.getZ()));
            Location safeLocation = new Location(to.getWorld(), safeX, to.getY(), safeZ, to.getYaw(), to.getPitch());
    
            event.getPlayer().teleport(safeLocation);
            event.getPlayer().sendMessage("You have reached the edge of the world!");
        }
    }
}

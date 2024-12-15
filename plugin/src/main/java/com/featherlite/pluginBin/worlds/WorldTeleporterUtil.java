package com.featherlite.pluginBin.worlds;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldTeleporterUtil {
    private final JavaPlugin plugin;

    public WorldTeleporterUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void teleportPlayer(Player player, Location location) {
        if (location.getWorld() == null) {
            plugin.getLogger().warning("Attempted to teleport player to a location without a world: " + location);
            player.sendMessage("Failed to teleport: The specified world is not loaded or does not exist.");
            return;
        }
        player.teleport(location);
    }
}

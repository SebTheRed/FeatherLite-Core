package com.featherlite.pluginBin.permissions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final PermissionManager permissionManager;

    public PlayerJoinListener(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Attach permissions on join
        permissionManager.attachPermissionsToPlayer(player);
    }
}

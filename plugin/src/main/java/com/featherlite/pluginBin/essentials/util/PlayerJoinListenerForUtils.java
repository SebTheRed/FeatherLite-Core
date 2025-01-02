package com.featherlite.pluginBin.essentials.util;


import com.featherlite.pluginBin.displays.DisplayPieceManager;
import com.featherlite.pluginBin.essentials.PlayerDataManager;
import com.featherlite.pluginBin.utils.InventoryManager;
import com.featherlite.pluginBin.utils.ColorUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListenerForUtils implements Listener {

    private final PlayerDataManager playerDataManager;
    private final DisplayPieceManager displayPieceManager;
    private final InventoryManager inventoryManager;

    public PlayerJoinListenerForUtils(PlayerDataManager playerDataManager, DisplayPieceManager displayPieceManager, InventoryManager inventoryManager) {
        this.playerDataManager = playerDataManager;
        this.displayPieceManager = displayPieceManager;
        this.inventoryManager = inventoryManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        String savedNickname = playerData.getString("nickname");

        // displayPieceManager.mountDisplayOnPlayer(player, ColorUtils.parseColors(savedNickname));

        if (savedNickname != null && !savedNickname.isEmpty()) {
            String formattedNickname = ColorUtils.parseColors(savedNickname);
            // player.setDisplayName(formattedNickname);
            player.setPlayerListName(formattedNickname);
        }

        if (inventoryManager.hasStoredInventory(player)) {
            Bukkit.getLogger().info("Restoring inventory for player: " + player.getName());
            inventoryManager.restoreInventory(player);
            player.sendMessage("§aYour saved inventory has been restored!");
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
    }
}

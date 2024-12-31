package com.featherlite.pluginBin.essentials.util;

import com.featherlite.pluginBin.displays.DisplayPieceManager;
import com.featherlite.pluginBin.essentials.PlayerDataManager;
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

    public PlayerJoinListenerForUtils(PlayerDataManager playerDataManager, DisplayPieceManager displayPieceManager) {
        this.playerDataManager = playerDataManager;
        this.displayPieceManager = displayPieceManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        String savedNickname = playerData.getString("nickname");

        displayPieceManager.mountDisplayOnPlayer(player, savedNickname);

        if (savedNickname != null && !savedNickname.isEmpty()) {
            String formattedNickname = ChatColor.translateAlternateColorCodes('&', savedNickname);
            player.setDisplayName(formattedNickname);
            player.setPlayerListName(formattedNickname);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        displayPieceManager.removePlayerDisplay(player);
    }
}

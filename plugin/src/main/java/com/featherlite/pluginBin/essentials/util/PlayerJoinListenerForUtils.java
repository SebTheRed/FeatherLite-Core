package com.featherlite.pluginBin.essentials.util;

import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListenerForUtils implements Listener {

    private final PlayerDataManager playerDataManager;

    public PlayerJoinListenerForUtils(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        String savedNickname = playerData.getString("nickname");

        if (savedNickname != null && !savedNickname.isEmpty()) {
            String formattedNickname = ChatColor.translateAlternateColorCodes('&', savedNickname);
            player.setDisplayName(formattedNickname);
            player.setPlayerListName(formattedNickname);
        }
    }
}

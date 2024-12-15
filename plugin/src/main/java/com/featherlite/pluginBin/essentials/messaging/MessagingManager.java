package com.featherlite.pluginBin.essentials.messaging;

import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class MessagingManager {
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, UUID> lastMessaged; // Track who last messaged whom

    public MessagingManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
        this.lastMessaged = new HashMap<>();
    }

    public boolean isMessageToggled(Player player) {
        return playerDataManager.getPlayerData(player).getBoolean("messaging.msgToggle", false);
    }

    public void toggleMessaging(Player player) {
        boolean current = isMessageToggled(player);
        playerDataManager.updatePlayerData(player, "messaging.msgToggle", !current);
    }

    public List<UUID> getIgnoredPlayers(Player player) {
        return playerDataManager.getUUIDList(player, "messaging.ignore");
    }

    public void toggleIgnore(Player player, UUID target) {
        List<UUID> ignored = getIgnoredPlayers(player);
        if (ignored.contains(target)) {
            ignored.remove(target);
        } else {
            ignored.add(target);
        }
        playerDataManager.setUUIDList(player, "messaging.ignore", ignored);
    }

    public void setLastMessaged(Player sender, Player receiver) {
        lastMessaged.put(sender.getUniqueId(), receiver.getUniqueId());
        playerDataManager.updatePlayerData(sender, "messaging.lastMessaged", receiver.getUniqueId().toString());
    }

    public UUID getLastMessaged(Player player) {
        String uuidString = playerDataManager.getPlayerData(player).getString("messaging.lastMessaged", null);
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }



}

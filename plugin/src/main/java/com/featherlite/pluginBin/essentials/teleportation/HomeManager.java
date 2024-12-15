package com.featherlite.pluginBin.essentials.teleportation;

import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeManager {
    private final PlayerDataManager playerDataManager;

    public HomeManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void setHome(Player player, String homeName, Location location) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        String homePath = "homes." + homeName;
        playerData.set(homePath + ".world", location.getWorld().getUID().toString());
        playerData.set(homePath + ".world-name", location.getWorld().getName());
        playerData.set(homePath + ".x", location.getX());
        playerData.set(homePath + ".y", location.getY());
        playerData.set(homePath + ".z", location.getZ());
        playerData.set(homePath + ".yaw", location.getYaw());
        playerData.set(homePath + ".pitch", location.getPitch());
        playerDataManager.savePlayerData(player, playerData);
    }

    public Location getHome(Player player, String homeName) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        if (!playerData.contains("homes." + homeName)) {
            return null;
        }
    
        String homePath = "homes." + homeName;
        String worldName = playerData.getString(homePath + ".world-name");
        double x = playerData.getDouble(homePath + ".x");
        double y = playerData.getDouble(homePath + ".y");
        double z = playerData.getDouble(homePath + ".z");
        float yaw = (float) playerData.getDouble(homePath + ".yaw");
        float pitch = (float) playerData.getDouble(homePath + ".pitch");
    
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public boolean deleteHome(Player player, String homeName) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        if (playerData.contains("homes." + homeName)) {
            playerData.set("homes." + homeName, null);
            playerDataManager.savePlayerData(player, playerData);
            return true;
        }
        return false;
    }

    public List<String> listHomes(Player player) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        if (!playerData.contains("homes")) {
            return new ArrayList<>();
        }
        return new ArrayList<>(playerData.getConfigurationSection("homes").getKeys(false));
    }
}

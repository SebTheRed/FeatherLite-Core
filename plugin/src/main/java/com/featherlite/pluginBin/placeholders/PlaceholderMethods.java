package com.featherlite.pluginBin.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlaceholderMethods {

    // 1. Online player count
    public static String getOnlinePlayerCount(Player player) {
        return String.valueOf(Bukkit.getOnlinePlayers().size());
    }

    // 2. Max player count
    public static String getMaxPlayerCount(Player player) {
        return String.valueOf(Bukkit.getMaxPlayers());
    }

    // 3. Player name
    public static String getPlayerName(Player player) {
        return player.getName();
    }

    // 4. Player health
    public static String getPlayerHealth(Player player) {
        return String.valueOf((int) player.getHealth());
    }

    // 5. Player maximum health
    public static String getPlayerMaxHealth(Player player) {
        return String.valueOf((int) player.getMaxHealth());
    }

    // 6. Player XP level
    public static String getPlayerXpLevel(Player player) {
        return String.valueOf(player.getLevel());
    }

    // 7. Current world name
    public static String getWorldName(Player player) {
        return player.getWorld().getName();
    }

    // 8. Current world time
    public static String getWorldTime(Player player) {
        long time = player.getWorld().getTime();
        return time < 12300 || time > 23850 ? "Day" : "Night"; // Day/Night cycle
    }

    // 9. Total loaded chunks in the current world
    public static String getLoadedChunks(Player player) {
        return String.valueOf(player.getWorld().getLoadedChunks().length);
    }

    // 10. Total entities in the current world
    public static String getEntityCount(Player player) {
        return String.valueOf(player.getWorld().getEntities().size());
    }

    // 11. Current server TPS (example with approximation)
    public static String getServerTps(Player player) {
        // Placeholder - Actual TPS retrieval may need NMS or external plugins.
        return "20.0"; // Assuming perfect TPS as a placeholder.
    }

    // // 12. Server uptime in hours
    // public static String getServerUptime(Player player) {
    //     long uptimeMillis = System.currentTimeMillis() - Bukkit.getServer().getStartTime();
    //     return String.format("%.1f", uptimeMillis / 3600000.0); // Convert ms to hours
    // }

    // 13. Player's current X coordinate
    public static String getPlayerX(Player player) {
        return String.format("%.2f", player.getLocation().getX());
    }

    // 14. Player's current Y coordinate
    public static String getPlayerY(Player player) {
        return String.format("%.2f", player.getLocation().getY());
    }

    // 15. Player's current Z coordinate
    public static String getPlayerZ(Player player) {
        return String.format("%.2f", player.getLocation().getZ());
    }

    // 16. Current date (e.g., "2024-11-17")
    public static String getCurrentDate(Player player) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }

    // 17. Current time (e.g., "14:35:00")
    public static String getCurrentTime(Player player) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date());
    }

    // 18. Player's ping/latency
    public static String getPlayerPing(Player player) {
        try {
            return String.valueOf(player.getPing()); // Requires Bukkit 1.17+
        } catch (Exception e) {
            return "N/A"; // If ping isn't available, return N/A
        }
    }

    // 19. Biome name where the player is currently located
    public static String getCurrentBiome(Player player) {
        return player.getLocation().getBlock().getBiome().toString();
    }

    // 20. Player's hunger level
    public static String getPlayerHunger(Player player) {
        return String.valueOf(player.getFoodLevel());
    }



}

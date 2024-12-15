package com.featherlite.pluginBin.placeholders;

import com.featherlite.pluginBin.zones.Zone;
import com.featherlite.pluginBin.zones.ZoneManager;
import org.bukkit.entity.Player;

public class PlaceholderZones {
    private static ZoneManager zoneManager;

    // Method to set the ZoneManager
    public static void setZoneManager(ZoneManager manager) {
        zoneManager = manager;
    }

    // Get the current zone name the player is in
    public static String getZoneName(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null ? zone.getName() : "No Zone";
    }

    // Get the current zone description the player is in
    public static String getZoneDescription(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null ? zone.getDescription() : "No Description";
    }

    // Get the current zone's world
    public static String getZoneWorld(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null ? zone.getWorld() : "No World";
    }

    // Get the entry message of the current zone
    public static String getZoneEntryMessage(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null ? zone.getEntryMessage() : "No Entry Message";
    }

    // Get the exit message of the current zone
    public static String getZoneExitMessage(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null ? zone.getExitMessage() : "No Exit Message";
    }

    // Get whether the current zone is a game zone
    public static String isGameZone(Player player) {
        Zone zone = getZoneForPlayer(player);
        return zone != null && zone.isGameZone() ? "true" : "false";
    }


    // Utility method to get the zone the player is currently in
    private static Zone getZoneForPlayer(Player player) {
        if (zoneManager == null) return null;
        return zoneManager.getZoneAtLocation(player.getLocation());
    }
}

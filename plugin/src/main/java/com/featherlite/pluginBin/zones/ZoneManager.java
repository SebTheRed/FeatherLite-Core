package com.featherlite.pluginBin.zones;

import com.featherlite.pluginBin.FeatherCore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class ZoneManager {
    private final FeatherCore plugin;
    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(FeatherCore plugin) {
        this.plugin = plugin;
        loadZones();
        saveDefaultSpawnFile();
        loadZonesFromOtherPlugins();  // New method to load zones from other plugins
    }
    


    private void loadZones() {
        File zonesFolder = new File(plugin.getDataFolder(), "zones");
        if (!zonesFolder.exists()) zonesFolder.mkdirs();

        for (File file : zonesFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                String zoneName = file.getName().replace(".yml", "");
                Zone zone = new Zone(zoneName, file);
                zones.put(zoneName, zone);
            }
        }
    }

    // New method to load zones from other plugins
    private void loadZonesFromOtherPlugins() {
        for (Plugin extPlugin : Bukkit.getPluginManager().getPlugins()) {
            File zonesFolder = new File(extPlugin.getDataFolder(), "zones");
            if (zonesFolder.exists() && zonesFolder.isDirectory()) {
                for (File file : zonesFolder.listFiles()) {
                    if (file.getName().endsWith(".yml")) {
                        String zoneName = file.getName().replace(".yml", "");
                        Zone zone = new Zone(zoneName, file);
                        zones.put(zoneName, zone); // Add the zone to the manager
                        plugin.getLogger().info("Zone " + zoneName + " loaded from " + extPlugin.getName());
                    }
                }
            }
        }
    }

    private void saveDefaultSpawnFile() {
        String folderName = "zones";
        String resourceFile = "spawn.yml";
    
        // Ensure the zones folder exists
        File zonesFolder = new File(plugin.getDataFolder(), folderName);
        if (!zonesFolder.exists()) {
            zonesFolder.mkdirs();
            plugin.getLogger().info(folderName + " directory did not exist. Created the directory.");
        }
    
        // Check if the spawn.yml file exists
        File spawnFile = new File(zonesFolder, resourceFile);
        if (!spawnFile.exists()) {
            plugin.getLogger().info(resourceFile + " not found! Saving default " + resourceFile + " to " + folderName + " directory...");
            plugin.saveResource(folderName + "/" + resourceFile, false);
            plugin.getLogger().info(resourceFile + " has been created successfully.");
        } else {
            plugin.getLogger().info(resourceFile + " already exists in " + folderName + ". Skipping save.");
        }
    }
    

    public void reloadAllZones() {
        zones.clear();  // Clear current zones
        loadZones();    // Reload zones from the plugin's data folder
        loadZonesFromOtherPlugins(); // Reload zones from other plugins
        plugin.getLogger().info("All zones reloaded successfully.");
    }

    public Zone getZone(String name) {
        return zones.get(name);
    }

    public Map<String, Zone> getZonesInWorld(String worldName) {
        Map<String, Zone> zonesInWorld = new HashMap<>();
        for (Map.Entry<String, Zone> entry : zones.entrySet()) {
            Zone zone = entry.getValue();
            // Match instance worlds by prefix (e.g., "Example_World_Name" matches "Example_World_Name_instance1")
            if (worldName.startsWith(zone.getWorld())) {
                zonesInWorld.put(entry.getKey(), zone);
            }
        }
        return zonesInWorld;
    }

    public Map<String, Zone> getZones() {
        return zones;
    }

    public Zone getZoneAtLocation(Location location) {
        return zones.values().stream()
                .filter(zone -> zone.isWithinBounds(location) && location.getWorld().getName().startsWith(zone.getWorld()))
                .findFirst()
                .orElse(null);
    }

    public void saveZone(Zone zone) {
        zone.saveConfig();
    }

    public void reloadZone(String name) {
        Zone zone = zones.get(name);
        if (zone != null) {
            File file = new File(plugin.getDataFolder(), "zones/" + name + ".yml");
            zones.put(name, new Zone(name, file));
        }
    }

    public void saveAllZones() {
        for (Zone zone : zones.values()) {
            zone.saveConfig();
        }
    }


    /**
     * Creates a new zone with unique name based on a provided base name.
     *
     * @param baseName       The base name of the zone (e.g., "arena", "safezone").
     * @param cornerOne      The first corner of the zone.
     * @param cornerTwo      The opposite corner of the zone.
     * @param entryMessage   Entry message for players entering the zone.
     * @param exitMessage    Exit message for players leaving the zone.
     * @return The created Zone object, or null if creation failed.
     */
    public Zone createZone(String baseName, Location cornerOne, Location cornerTwo, String entryMessage, String exitMessage) {
        // Generate a unique name
        String uniqueName = generateUniqueZoneName(baseName);

        // Create zone file
        File zoneFile = new File(plugin.getDataFolder(), "zones/" + uniqueName + ".yml");
        if (zoneFile.exists()) {
            plugin.getLogger().warning("Zone file already exists for " + uniqueName);
            return null;
        }

        try {
            zoneFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(zoneFile);

            // Set default configuration for new zone
            config.set("information.name", uniqueName);
            config.set("information.world", cornerOne.getWorld().getName()); // Set the world name here

            config.set("information.description", "Newly created zone.");
            config.set("coordinates.corner-one.x", cornerOne.getX());
            config.set("coordinates.corner-one.y", cornerOne.getY());
            config.set("coordinates.corner-one.z", cornerOne.getZ());
            config.set("coordinates.corner-one.world", cornerOne.getWorld().getName());

            config.set("coordinates.corner-two.x", cornerTwo.getX());
            config.set("coordinates.corner-two.y", cornerTwo.getY());
            config.set("coordinates.corner-two.z", cornerTwo.getZ());
            config.set("coordinates.corner-two.world", cornerTwo.getWorld().getName());

            config.set("player-rules.entry-message", entryMessage != null ? entryMessage : "Welcome to " + uniqueName);
            config.set("player-rules.exit-message", exitMessage != null ? exitMessage : "Goodbye from " + uniqueName);

            // Save configuration
            config.save(zoneFile);

            // Create and register the zone
            Zone newZone = new Zone(uniqueName, zoneFile);
            zones.put(uniqueName, newZone);
            plugin.getLogger().info("Zone " + uniqueName + " created successfully.");

            return newZone;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create zone file for " + uniqueName);
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Deletes a zone by name and removes its file.
     *
     * @param zoneName The name of the zone to delete.
     * @return true if the zone was successfully deleted; false otherwise.
     */
    public boolean deleteZone(String zoneName) {
        Zone zone = zones.remove(zoneName);
        if (zone == null) return false;

        File zoneFile = new File(plugin.getDataFolder(), "zones/" + zoneName + ".yml");
        if (zoneFile.exists()) {
            return zoneFile.delete();
        }
        return true;
    }

    /**
     * Generates a unique zone name using the base name and current timestamp.
     * Format: <baseName>_<timestamp>
     */
    private String generateUniqueZoneName(String baseName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd'T'HHmmss").format(new Date());
        return baseName + "_" + timestamp;
    }
}

package com.featherlite.pluginBin.essentials.teleportation;

import com.featherlite.pluginBin.essentials.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportationManager {
    private final PlayerDataManager playerDataManager;
    private final Map<UUID, TeleportRequest> teleportRequests = new HashMap<>(); // <Target, Request Details>
    private JavaPlugin plugin;
    private final File worldsFile;
    private final YamlConfiguration worldsConfig;

    public TeleportationManager(PlayerDataManager playerDataManager, JavaPlugin plugin) {
        this.playerDataManager = playerDataManager;
        this.plugin = plugin;
        this.worldsFile = new File(plugin.getDataFolder(), "worlds-data.yml");
        this.worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);

        // Create file if it doesn't exist
        if (!worldsFile.exists()) {
            try {
                worldsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create worlds-data.yml file!");
                e.printStackTrace();
            }
        }
    }

    // Inner class to represent a teleport request
    private static class TeleportRequest {
        private final UUID requesterId;
        private final RequestType requestType;

        public TeleportRequest(UUID requesterId, RequestType requestType) {
            this.requesterId = requesterId;
            this.requestType = requestType;
        }

        public UUID getRequesterId() {
            return requesterId;
        }

        public RequestType getRequestType() {
            return requestType;
        }
    }

    // Enum to differentiate between TPA and TPAHERE
    private enum RequestType {
        TPA, TPAHERE
    }

    public boolean requestTeleport(Player requester, Player target) {
        if (teleportRequests.containsKey(target.getUniqueId())) {
            requester.sendMessage(ChatColor.RED + "This player already has a pending teleport request.");
            return false;
        }

        teleportRequests.put(target.getUniqueId(), new TeleportRequest(requester.getUniqueId(), RequestType.TPA));
        requester.sendMessage(ChatColor.GREEN + "Teleport request sent to " + target.getName());
        target.sendMessage(ChatColor.YELLOW + requester.getName() + " has requested to teleport to you. Use /tpaccept or /tpdeny.");
        return true;
    }

    public boolean requestTeleportHere(Player requester, Player target) {
        if (teleportRequests.containsKey(target.getUniqueId())) {
            requester.sendMessage(ChatColor.RED + "This player already has a pending teleport request.");
            return false;
        }

        teleportRequests.put(target.getUniqueId(), new TeleportRequest(requester.getUniqueId(), RequestType.TPAHERE));
        requester.sendMessage(ChatColor.GREEN + "Teleport request sent to " + target.getName());
        target.sendMessage(ChatColor.YELLOW + requester.getName() + " has requested you to teleport to them. Use /tpaccept or /tpdeny.");
        return true;
    }

    public boolean acceptTeleport(Player target) {
        UUID targetId = target.getUniqueId();

        if (!teleportRequests.containsKey(targetId)) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return false;
        }

        TeleportRequest request = teleportRequests.get(targetId);
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester == null) {
            target.sendMessage(ChatColor.RED + "The player who requested the teleport is offline.");
            teleportRequests.remove(targetId);
            return false;
        }

        switch (request.getRequestType()) {
            case TPA:
                saveLastLocation(requester); // Save the requester's location
                requester.teleport(target.getLocation());
                requester.sendMessage(ChatColor.GREEN + "You have been teleported to " + target.getName());
                target.sendMessage(ChatColor.YELLOW + requester.getName() + " has teleported to you.");
                break;

            case TPAHERE:
                saveLastLocation(target); // Save the target's location
                target.teleport(requester.getLocation());
                target.sendMessage(ChatColor.GREEN + "You have been teleported to " + requester.getName());
                requester.sendMessage(ChatColor.YELLOW + target.getName() + " has teleported to you.");
                break;
        }

        teleportRequests.remove(targetId);
        return true;
    }

    public boolean denyTeleport(Player target) {
        UUID targetId = target.getUniqueId();

        if (!teleportRequests.containsKey(targetId)) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return false;
        }

        TeleportRequest request = teleportRequests.get(targetId);
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester != null) {
            requester.sendMessage(ChatColor.YELLOW + target.getName() + " has denied your teleport request.");
        }

        target.sendMessage(ChatColor.RED + "You denied the teleport request.");
        teleportRequests.remove(targetId);
        return true;
    }

    public boolean cancelTeleport(Player requester) {
        UUID requesterId = requester.getUniqueId();
    
        // Find the target associated with this request
        UUID targetId = teleportRequests.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getRequesterId().equals(requesterId))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
    
        if (targetId == null) {
            requester.sendMessage(ChatColor.RED + "You have no pending teleport requests to cancel.");
            return false;
        }
    
        Player target = Bukkit.getPlayer(targetId);
        if (target != null) {
            target.sendMessage(ChatColor.YELLOW + requester.getName() + " has canceled their teleport request.");
        }
    
        teleportRequests.remove(targetId);
        requester.sendMessage(ChatColor.GREEN + "Your teleport request has been canceled.");
        return true;
    }
    













    public void saveLastLocation(Player player) {
        Location location = player.getLocation();
    
        // Create a map of updates
        Map<String, Object> updates = new HashMap<>();
        updates.put("user-stats.last-teleport.world-name", location.getWorld().getName());
        updates.put("user-stats.last-teleport.x", location.getX());
        updates.put("user-stats.last-teleport.y", location.getY());
        updates.put("user-stats.last-teleport.z", location.getZ());
        updates.put("user-stats.last-teleport.yaw", location.getYaw());
        updates.put("user-stats.last-teleport.pitch", location.getPitch());
    
        // Perform batch update
        playerDataManager.updatePlayerDataBatch(player, updates);
    }

    public boolean teleportBack(Player player) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);

        String worldName = playerData.getString("user-stats.last-teleport.world-name");
        double x = playerData.getDouble("user-stats.last-teleport.x");
        double y = playerData.getDouble("user-stats.last-teleport.y");
        double z = playerData.getDouble("user-stats.last-teleport.z");
        float yaw = (float) playerData.getDouble("user-stats.last-teleport.yaw");
        float pitch = (float) playerData.getDouble("user-stats.last-teleport.pitch");

        if (worldName == null) {
            player.sendMessage(ChatColor.RED + "No last teleport location found.");
            return false;
        }

        Location lastLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        player.teleport(lastLocation);
        player.sendMessage(ChatColor.GREEN + "Teleported to your last location.");
        return true;
    }

    // public boolean requestTeleport(Player requester, Player target) {
    //     if (teleportRequests.containsKey(target.getUniqueId())) {
    //         requester.sendMessage("You already have a pending teleport request to this player.");
    //         return false;
    //     }

    //     teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
    //     requester.sendMessage("Teleport request sent to " + target.getName());
    //     target.sendMessage(requester.getName() + " has requested to teleport to you. Use /tpaccept or /tpdeny.");
    //     return true;
    // }

    // public boolean requestTeleportHere(Player requester, Player target) {
    //     if (teleportRequests.containsKey(target.getUniqueId())) {
    //         requester.sendMessage(ChatColor.RED + "This player already has a pending teleport request.");
    //         return false;
    //     }
    
    //     teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
    //     requester.sendMessage(ChatColor.GREEN + "Teleport request sent to " + target.getName());
    //     target.sendMessage(ChatColor.YELLOW + requester.getName() + " has requested you to teleport to them. Use /tpaccept or /tpdeny.");
    //     return true;
    // }

    // public boolean cancelTeleport(Player requester) {
    //     UUID requesterId = requester.getUniqueId();
    
    //     // Check if the requester has any pending teleport requests
    //     UUID targetId = teleportRequests.entrySet()
    //         .stream()
    //         .filter(entry -> entry.getValue().equals(requesterId))
    //         .map(Map.Entry::getKey)
    //         .findFirst()
    //         .orElse(null);
    
    //     if (targetId == null) {
    //         requester.sendMessage(ChatColor.RED + "You have no pending teleport requests to cancel.");
    //         return false;
    //     }
    
    //     Player target = Bukkit.getPlayer(targetId);
    //     if (target != null) {
    //         target.sendMessage(ChatColor.YELLOW + requester.getName() + " has canceled their teleport request.");
    //     }
    
    //     teleportRequests.remove(targetId);
    //     requester.sendMessage(ChatColor.GREEN + "Your teleport request has been canceled.");
    //     return true;
    // }

    public boolean acceptTpa(Player target) {
        UUID targetId = target.getUniqueId();
    
        if (!teleportRequests.containsKey(targetId)) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return false;
        }
    
        TeleportRequest request = teleportRequests.get(targetId);
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester == null) {
            target.sendMessage(ChatColor.RED + "The requester is no longer online.");
            teleportRequests.remove(targetId);
            return false;
        }
    
        saveLastLocation(requester); // Save the requester's previous location
        requester.teleport(target.getLocation());
        requester.sendMessage(ChatColor.GREEN + "You have been teleported to " + target.getName());
        target.sendMessage(ChatColor.YELLOW + requester.getName() + " has teleported to you.");
        teleportRequests.remove(targetId);
        return true;
    }
    

    public boolean acceptTpahere(Player target) {
        UUID targetId = target.getUniqueId();
    
        if (!teleportRequests.containsKey(targetId)) {
            target.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
            return false;
        }
    
        TeleportRequest request = teleportRequests.get(targetId);
        Player requester = Bukkit.getPlayer(request.getRequesterId());
        if (requester == null) {
            target.sendMessage(ChatColor.RED + "The requester is no longer online.");
            teleportRequests.remove(targetId);
            return false;
        }
    
        saveLastLocation(target); // Save the target's previous location
        target.teleport(requester.getLocation());
        target.sendMessage(ChatColor.GREEN + "You have been teleported to " + requester.getName());
        requester.sendMessage(ChatColor.YELLOW + target.getName() + " has teleported to you.");
        teleportRequests.remove(targetId);
        return true;
    }

    // public boolean denyTeleport(Player target) {
    //     UUID targetId = target.getUniqueId();

    //     if (!teleportRequests.containsKey(targetId)) {
    //         target.sendMessage("You have no pending teleport requests.");
    //         return false;
    //     }

    //     Player requester = Bukkit.getPlayer(teleportRequests.get(targetId));
    //     if (requester != null) {
    //         requester.sendMessage(target.getName() + " has denied your teleport request.");
    //     }

    //     target.sendMessage("You denied the teleport request.");
    //     teleportRequests.remove(targetId);
    //     return true;
    // }

    public boolean teleportRandomly(Player player, int minRadius, int maxRadius) {
        // UUID playerId = player.getUniqueId();
        Location spawnLocation = player.getWorld().getSpawnLocation(); // Use world spawn as a center point
        int attempts = 10; // Maximum attempts to find a valid location
    
        for (int i = 0; i < attempts; i++) {
            // Generate random coordinates within the range
            double randomX = spawnLocation.getX() + (Math.random() * (maxRadius - minRadius) + minRadius) * (Math.random() < 0.5 ? -1 : 1);
            double randomZ = spawnLocation.getZ() + (Math.random() * (maxRadius - minRadius) + minRadius) * (Math.random() < 0.5 ? -1 : 1);
    
            // Get the highest block at the location
            Location randomLocation = new Location(player.getWorld(), randomX, 0, randomZ);
            int highestY = player.getWorld().getHighestBlockYAt(randomLocation); // Get the Y-coordinate of the highest block
            randomLocation.setY(highestY); // Set the Y-coordinate of the random location

            // Check if the block is safe
            if (isSafeLocation(randomLocation)) {
                player.teleport(randomLocation);
                saveLastLocation(player); // Save the teleport location
                // player.sendMessage(ChatColor.GREEN + "You have been teleported to a random location!");
                return true;
            }
        }
    
        player.sendMessage(ChatColor.RED + "Could not find a safe location after " + attempts + " attempts.");
        return false;
    }

    public boolean teleportToSpawn(Player player) {
        Location spawnLocation = getServerSpawn(); // Get spawn from worlds-data.yml
    
        if (spawnLocation == null || spawnLocation.getWorld() == null) {
            player.sendMessage(ChatColor.RED + "Server spawn is not set or the world is not loaded.");
            return false;
        }
    
        saveLastLocation(player);
        player.teleport(spawnLocation);
        player.sendMessage(ChatColor.GREEN + "You have been teleported to the server spawn.");
        return true;
    }
    
    public Location getServerSpawn() {
        String worldName = worldsConfig.getString("server-spawn.world");
        if (worldName == null) return null;

        double x = worldsConfig.getDouble("server-spawn.x");
        double y = worldsConfig.getDouble("server-spawn.y");
        double z = worldsConfig.getDouble("server-spawn.z");
        float yaw = (float) worldsConfig.getDouble("server-spawn.yaw", 0);
        float pitch = (float) worldsConfig.getDouble("server-spawn.pitch", 0);

        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public boolean setServerSpawn(Player player, Location location) {
        worldsConfig.set("server-spawn.world", location.getWorld().getName());
        worldsConfig.set("server-spawn.x", location.getX());
        worldsConfig.set("server-spawn.y", location.getY());
        worldsConfig.set("server-spawn.z", location.getZ());
        worldsConfig.set("server-spawn.yaw", location.getYaw());
        worldsConfig.set("server-spawn.pitch", location.getPitch());

        try {
            worldsConfig.save(worldsFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save spawn location to worlds-data.yml");
            e.printStackTrace();
            return false;
        }
        player.sendMessage(ChatColor.GREEN + "You have set the server spawn!.");
        return true;
    }


    
    private boolean isSafeLocation(Location location) {
        // Check if the block and block below are solid, and not water or lava
        return location.getBlock().getType().isSolid() &&
               location.clone().add(0, -1, 0).getBlock().getType().isSolid() &&
               !location.getBlock().isLiquid();
    }
    
    public Map<UUID, TeleportRequest> getTeleportRequests() {
        return teleportRequests;
    }


}

package com.featherlite.pluginBin.permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.featherlite.pluginBin.essentials.PlayerDataManager;

import java.util.*;

public class PermissionManager {
    private final PermissionsFileManager fileManager;
    private final PlayerDataManager playerDataManager;
    private final JavaPlugin plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();
    private FileConfiguration groupConfig;

    public PermissionManager(JavaPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.fileManager = new PermissionsFileManager(plugin);

        // Load the group configuration using the FileManager
        this.groupConfig = fileManager.loadConfig("permission-groups.yml");
    }

    // Add a player to a group
    public void addPlayerToGroup(String playerName, String groupName, CommandSender sender) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            target.sendMessage("Player not found.");
            return;
        }

        FileConfiguration playerData = playerDataManager.getPlayerData(target);
        List<String> groups = playerData.getStringList("permissions.groups");

        if (!groups.contains(groupName)) {
            groups.add(groupName);
            playerData.set("permissions.groups", groups);
            // fileManager.savePlayerConfig(playerConfig, target.getName(), target.getUniqueId().toString());
            playerDataManager.savePlayerData(target, playerData);
            attachPermissionsToPlayer(target);
            sender.sendMessage("Player " + target + " added to group: " + groupName);
        } else {
            sender.sendMessage("Player is already in this group.");
        }
    }

    // Remove a player from a group
    public void removePlayerFromGroup(String playerName, String groupName, CommandSender sender) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        // FileConfiguration playerConfig = fileManager.loadPlayerConfig(target.getName(), target.getUniqueId().toString());
        FileConfiguration playerData = playerDataManager.getPlayerData(target);
        List<String> groups = playerData.getStringList("permissions.groups");

        if (groups.remove(groupName)) {
            playerData.set("permissions.groups", groups);
            // fileManager.savePlayerConfig(playerConfig, target.getName(), target.getUniqueId().toString());
            playerDataManager.savePlayerData(target, playerData);
            attachPermissionsToPlayer(target);
            sender.sendMessage("Player " + target + " removed from group: " + groupName);
        } else {
            sender.sendMessage("Player is not in this group.");
        }
    }

    // Retrieve all permissions for a group, including inherited permissions
    private Set<String> getGroupPermissions(String groupName) {
        return resolveGroupPermissions(groupName, new HashSet<>());
    }
    
    private Set<String> resolveGroupPermissions(String groupName, Set<String> visitedGroups) {
        // Prevent infinite loops by checking if the group was already visited
        if (visitedGroups.contains(groupName)) {
            plugin.getLogger().warning("Detected cyclic inheritance for group: " + groupName);
            return Collections.emptySet(); // Return an empty set to break the cycle
        }
    
        // Mark the current group as visited
        visitedGroups.add(groupName);
    
        Set<String> permissions = new HashSet<>();
        List<String> groupPermissions = groupConfig.getStringList("permission-groups." + groupName);
    
        if (groupPermissions != null && !groupPermissions.isEmpty()) {
            for (String permission : groupPermissions) {
                if (permission.startsWith("group.")) {
                    // Handle inherited groups
                    String inheritedGroupName = permission.substring(6); // Remove "group." prefix
                    permissions.addAll(resolveGroupPermissions(inheritedGroupName, visitedGroups));
                } else {
                    // Add the regular permission
                    permissions.add(permission);
                }
            }
        } else {
            plugin.getLogger().warning("Group " + groupName + " not found or has no permissions.");
        }
    
        // plugin.getLogger().info("Permissions for group " + groupName + ": " + permissions); // REACTIVATE FOR DEBUGGER
        return permissions;
    }
    
    
    

    // Get all effective permissions for a player
    public Set<String> getEffectivePermissions(Player player) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        Set<String> effectivePermissions = new HashSet<>();

        // Add permissions from all groups
        List<String> playerGroups = playerData.getStringList("permissions.groups");
        if (playerGroups == null) playerGroups = new ArrayList<>();

        for (String group : playerGroups) {
            effectivePermissions.addAll(getGroupPermissions(group));
        }

        // Add individual permissions
        effectivePermissions.addAll(playerData.getStringList("permissions.individual"));

        return effectivePermissions;
    }

    // Get a list of all effective permissions for a player (for display in commands)
    public List<String> getPlayerEffectivePermissions(Player player) {
        return new ArrayList<>(getEffectivePermissions(player));
    }

    // Set a specific permission for a player
    public void setPlayerPermission(String playerName, String permission, boolean value, CommandSender sender) {
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        // Load player data using PlayerDataManager
        FileConfiguration playerData = playerDataManager.getPlayerData(target);
        List<String> individualPermissions = playerData.getStringList("permissions.individual");
        if (individualPermissions == null) individualPermissions = new ArrayList<>();

        if (value) {
            if (!individualPermissions.contains(permission)) {
                individualPermissions.add(permission); // Add permission if it doesn't already exist
            }
        } else {
            individualPermissions.remove(permission); // Remove permission if it exists
        }

        // Update the individual permissions in the player data
        playerData.set("permissions.individual", individualPermissions);
        playerDataManager.savePlayerData(target, playerData);

        // Reattach permissions to apply changes
        attachPermissionsToPlayer(target);

        sender.sendMessage("Set permission '" + permission + "' to " + value + " for player " + target.getName());
    }


    // Retrieve groups for a player
    public List<String> getPlayerGroups(Player player) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        return playerData.getStringList("permissions.groups");
    }

    // Retrieve individual permissions for a player
    public List<String> getPlayerPermissions(Player player) {
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        return playerData.getStringList("permissions.individual");
    }

    // Get a list of all available groups
    public List<String> getAvailableGroups() {
        if (groupConfig.contains("permission-groups")) {
            return new ArrayList<>(groupConfig.getConfigurationSection("permission-groups").getKeys(false));
        }
        return Collections.emptyList();
    }

    // Attach permissions to a player based on their effective permissions (from groups and individual permissions)
    public void attachPermissionsToPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        PermissionAttachment attachment;

        if (attachments.containsKey(playerUUID)) {
            attachment = attachments.get(playerUUID);
        } else {
            attachment = player.addAttachment(plugin);
            attachments.put(playerUUID, attachment);
        }

        // Clear existing permissions
        attachment.getPermissions().keySet().forEach(permission -> attachment.unsetPermission(permission));

        // Assign default groups if the player is new (no permissions.groups data)
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        List<String> playerGroups = playerData.getStringList("permissions.groups");

        if (playerGroups == null || playerGroups.isEmpty()) {
            List<String> defaultGroups = plugin.getConfig().getStringList("first-join-permission-groups");
            playerData.set("permissions.groups", defaultGroups);
            playerDataManager.savePlayerData(player, playerData);
            plugin.getLogger().info("Assigned default groups to new player: " + player.getName());
        }

        // Apply effective permissions
        Set<String> effectivePermissions = getEffectivePermissions(player);
        for (String permission : effectivePermissions) {
            attachment.setPermission(permission, true);
        }

        plugin.getLogger().info("Permissions attached for player: " + player.getName());
    }



    // Attach permissions to all currently online players
    public void attachPermissionsToAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            attachPermissionsToPlayer(player);
        }
    }

    // Reload configuration and reattach permissions
    public void reloadConfig() {
        groupConfig = fileManager.loadConfig("permission-groups.yml");
        attachPermissionsToAllOnlinePlayers();
        plugin.getLogger().info("Configuration reloaded and permissions refreshed for all online players.");
    }


    public void showAllServerPermissions(Player player) {
        PluginManager pluginManager = Bukkit.getPluginManager();
        List<String> permissionNames = new ArrayList<>();
        pluginManager.getPermissions().forEach(permission -> permissionNames.add(permission.getName()));
    
        if (permissionNames.isEmpty()) {
            player.sendMessage("No permissions registered on this server.");

        }

        player.sendMessage("Registered Permissions:");
        
        // Send permissions in batches of 10 to avoid flooding the chat
        int batchSize = 10;
        for (int i = 0; i < permissionNames.size(); i += batchSize) {
            int end = Math.min(i + batchSize, permissionNames.size());
            List<String> batch = permissionNames.subList(i, end);
            player.sendMessage(String.join(", ", batch));
        }

    
    }
        
    

}

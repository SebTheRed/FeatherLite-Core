package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.featherlite.pluginBin.FeatherCore;
import com.featherlite.pluginBin.worlds.WorldManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Random;

public class InstanceManager {
    private final FeatherCore plugin;
    private final Map<UUID, GameInstance> activeInstances = new HashMap<>();
    private final PartyManager partyManager;
    private final WorldManager worldManager;
    private int instanceCount = 0; // Track unique instance numbers

    public InstanceManager(PartyManager partyManager, WorldManager worldManager, FeatherCore plugin) {
        this.partyManager = partyManager;
        this.worldManager = worldManager;
        this.plugin = plugin;
    }

    /**
     * Creates a new GameInstance.
     *
     * @param gameName        The name of the game (e.g., "BedWars").
     * @param gameType        The type of the game (e.g., "SkyWars", "CaptureTheFlag").
     * @param baseWorldName   The name of the base/template world to copy.
     * @param teamSizes       The team names and their sizes.
     * @param teamNames       A list of team names (e.g., ["Red", "Blue", "Green"]).
     * @param teamSpawns      A map of team names to spawn locations.
     * @param pluginConfig    A plugin-specific configuration object, or null if not needed.
     * @return The created GameInstance, or null if the creation failed.
     */
    public GameInstance createInstance(
            String gameName,
            String gameType,
            String baseWorldName,
            Map<String, Integer> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Location> teamSpawns,
            Object pluginConfig // Plugin-specific data, optional
    ) {
        // Generate a unique world name for the new instance
        String newWorldName = baseWorldName + "_instance" + (++instanceCount);

        // Use the WorldManager to create and load the instance world
        World instanceWorld = worldManager.createInstanceWorld(baseWorldName, newWorldName);
        if (instanceWorld == null) {
            plugin.getLogger().warning("Failed to create instance world: " + newWorldName);
            return null;
        }

        // Create a new game instance object
        GameInstance instance = new GameInstance(
                gameName,
                gameType,
                newWorldName,
                teamSizes,
                maxTime,
                teamNames,
                teamSpawns,
                pluginConfig // Pass plugin-specific config
        );
        instance.setWorldName(newWorldName);

        // Store the active instance in the manager
        activeInstances.put(instance.getInstanceId(), instance);

        // Start the instance timer
        startInstanceTimer(instance);

        plugin.getLogger().info("Game instance created successfully: " + newWorldName);
        return instance;
    };
    

    public GameInstance getInstance(UUID instanceId) {
        return activeInstances.get(instanceId);
    };

    public void removeInstance(UUID instanceId) {
        GameInstance instance = activeInstances.remove(instanceId);
        if (instance != null) {
            // Unload and delete the instance-specific world
            worldManager.deleteWorld(instance.getWorldName());
            plugin.getLogger().info("Successfully removed and deleted instance world: " + instance.getWorldName());
        } else {
            plugin.getLogger().warning("Attempted to remove an invalid or non-existent instance with ID: " + instanceId);
        }
    };

    public void closeInstance(UUID instanceId, boolean broadcastMessage, String lobbyName) {
        GameInstance instance = activeInstances.remove(instanceId);
        if (instance == null) {
            plugin.getLogger().warning("Attempted to close an invalid or non-existent instance with ID: " + instanceId);
            return;
        }

        // Broadcast a message if specified
        if (broadcastMessage) {
            instance.broadcastToAllPlayers("This game instance is being closed. You will be teleported back to the lobby.");
        }

        // Teleport all players safely to the specified lobby
        teleportPlayersToSafeLocation(instance, lobbyName);

        // End the game if it's still in progress
        if (instance.getState() == GameInstance.GameState.IN_PROGRESS) {
            instance.endGame();
        }

        // Unload and delete the instance-specific world
        worldManager.deleteWorld(instance.getWorldName());
        plugin.getLogger().info("Successfully closed and removed instance: " + instanceId);
    };

    private void teleportPlayersToSafeLocation(GameInstance instance, String lobbyName) {
        // Fetch the specified lobby location from the core plugin
        final Location safeLocation = plugin.getLobbyLocation(lobbyName);
        Location backupLocation = Bukkit.getWorld("world").getSpawnLocation();
    
        // Iterate through all teams and teleport their members to the safe location
        instance.getTeams().values().forEach(playerList -> playerList.forEach(playerUUID -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                if (safeLocation == null) {
                    player.teleport(backupLocation);
                } else {
                    player.teleport(safeLocation);
                }
                player.sendMessage("You have been teleported back to the lobby: " + lobbyName);
            }
        }));
    
        // Optionally, handle spectators if they exist
        instance.getSpectators().forEach(spectatorUUID -> {
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null && spectator.isOnline()) {
                if (safeLocation == null) {
                    spectator.teleport(backupLocation);
                } else {
                    spectator.teleport(safeLocation);
                }
                spectator.sendMessage("You have been teleported back to the lobby: " + lobbyName);
            }
        });
    };
    

    private void startInstanceTimer(GameInstance instance) {
        // Schedule a task to end the game when maxTime expires
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (instance.getState() == GameInstance.GameState.IN_PROGRESS) {
                instance.broadcastToAllPlayers("Time's up! The game has ended.");
                instance.endGame();
            }
        }, instance.getMaxTime() * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    };

    public void teleportTeamPlayers(GameInstance instance, String team, Player player) {
        Location spawnLocation = instance.getTeamSpawns().get(team);
        if (spawnLocation != null) {
            worldManager.teleportPlayer(player, spawnLocation);
        }
    };
    


    public void addPlayerToInstance(Player player, GameInstance instance, String teamName) {
        Party playerParty = partyManager.getParty(player);
    
        // If the player is in a party, check the party's presence in other instances
        if (playerParty != null) {
            // Ensure the party is not already in another instance
            if (isPartyInAnotherInstance(playerParty)) {
                player.sendMessage("Your party is already in another game instance.");
                return;
            }
    
            // Attempt to add the entire party to a single team
            boolean success = addPartyToSingleOrMultipleTeams(instance, playerParty);
            if (!success) {
                player.sendMessage("Could not fit your party into a single team or multiple teams. Please try again.");
            }
            return;
        }
    
        // If the player is not in a party, assign them to a random team
        String assignedTeam = teamName != null ? teamName : assignPlayerToRandomTeam(instance);
        if (assignedTeam == null) {
            player.sendMessage("No available teams found to join.");
            return;
        }
    
        // Add the player to the assigned team
        if (instance.isFull()) {
            player.sendMessage("The instance is full. Cannot join.");
            return;
        }
    
        instance.getTeams().get(assignedTeam).add(player.getUniqueId());
    
        // Teleport the player to the assigned team's spawn location
        Location spawnLocation = instance.getTeamSpawns().get(assignedTeam);
        if (spawnLocation != null) {
            worldManager.teleportPlayer(player, spawnLocation);
        }
    
        instance.broadcastToAllPlayers(player.getName() + " has joined the " + assignedTeam + " team!");
    };

    private boolean addPartyToSingleOrMultipleTeams(GameInstance instance, Party party) {
        List<String> availableTeams = getAvailableTeams(instance, party.getMembers().size());
    
        if (availableTeams.isEmpty()) {
            return false; // No available teams with enough space for the entire party
        }
    
        int partySize = party.getMembers().size();
        int remainingMembers = partySize;
        int teamIndex = 0;
    
        // Distribute party members across multiple teams if needed
        for (UUID memberUUID : party.getMembers()) {
            String currentTeam = availableTeams.get(teamIndex);
            instance.getTeams().get(currentTeam).add(memberUUID);
    
            // Check if the current team is now full
            if (instance.getTeams().get(currentTeam).size() >= instance.getTeamSizes().get(currentTeam)) {
                teamIndex++; // Move to the next available team
            }
    
            remainingMembers--;
    
            // Teleport members to their assigned team spawn
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                teleportTeamPlayers(instance, currentTeam, member);
            }
        }
    
        return remainingMembers == 0; // Success if no remaining members
    }
    
    
    private List<String> getAvailableTeams(GameInstance instance, int requiredSlots) {
        List<String> availableTeams = new ArrayList<>();
    
        for (Map.Entry<String, List<UUID>> entry : instance.getTeams().entrySet()) {
            String teamName = entry.getKey();
            int currentSize = entry.getValue().size();
            int maxSize = instance.getTeamSizes().get(teamName); // Get max size for this team
    
            int availableSlots = maxSize - currentSize;
            if (availableSlots >= requiredSlots) {
                availableTeams.add(teamName);
            }
        }
    
        return availableTeams;
    }
    


    // Method to check if a player is part of an active instance
    public boolean isPartyInAnotherInstance(Party party) {
        for (GameInstance instance : activeInstances.values()) {
            for (UUID memberUUID : party.getMembers()) {
                if (isPlayerInInstance(instance, memberUUID)) {
                    return true; // One of the party members is already in an active instance
                }
            }
        }
        return false;
    };

    // Helper method to check if a specific player is in a given instance
    private boolean isPlayerInInstance(GameInstance instance, UUID playerUUID) {
        for (List<UUID> teamMembers : instance.getTeams().values()) {
            if (teamMembers.contains(playerUUID)) {
                return true;
            }
        }
        return false;
    };

    // Assign an individual player to a random team with available slots
    private String assignPlayerToRandomTeam(GameInstance instance) {
        List<String> availableTeams = new ArrayList<>();
    
        for (Map.Entry<String, List<UUID>> entry : instance.getTeams().entrySet()) {
            String teamName = entry.getKey();
            int currentSize = entry.getValue().size();
            int maxSize = instance.getTeamSizes().get(teamName); // Get max size for this team
    
            if (currentSize < maxSize) {
                availableTeams.add(teamName); // Add team to the pool if it has available slots
            }
        }
    
        if (availableTeams.isEmpty()) {
            return null; // No available teams found
        }
    
        // Pick a random team from available ones
        return availableTeams.get(new Random().nextInt(availableTeams.size()));
    }
    
    
    public void handlePlayerLeave(Player player) {
        for (GameInstance instance : activeInstances.values()) {
            // Iterate through each team to find and remove the player
            for (Map.Entry<String, List<UUID>> entry : instance.getTeams().entrySet()) {
                if (entry.getValue().remove(player.getUniqueId())) {
                    instance.broadcastToAllPlayers(player.getName() + " has left the game.");
                    return;
                }
            }
        }
    };
    
    public void handlePlayerDisconnect(Player player) {
        // Similar to handlePlayerLeave, but you can handle it differently if needed
        handlePlayerLeave(player);
        // Optionally track disconnected players if you want to allow rejoining
    };


    public GameInstance getInstanceForPlayer(Player player) {
        for (GameInstance instance : activeInstances.values()) {
            for (List<UUID> teamMembers : instance.getTeams().values()) {
                if (teamMembers.contains(player.getUniqueId())) {
                    return instance;
                }
            }
        }
        return null; // Player is not in any active instance
    }

}

package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.featherlite.pluginBin.FeatherCore;
import com.featherlite.pluginBin.worlds.WorldManager;
import com.featherlite.pluginBin.lobbies.TeamSelectorBook;
import com.featherlite.pluginBin.utils.InventoryManager;

import java.util.ArrayList;
import java.util.Collections;
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
    private final TeamSelectorBook teamSelectorBook;
    private int instanceCount = 0; // Track unique instance numbers

    public InstanceManager(PartyManager partyManager, WorldManager worldManager, FeatherCore plugin, TeamSelectorBook teamSelectorBook) {
        this.partyManager = partyManager;
        this.worldManager = worldManager;
        this.plugin = plugin;
        this.teamSelectorBook = teamSelectorBook;
    }

    /**
     * Creates a new GameInstance.
     * @param isInstancePublic Is this instance open to the public or is invite only?
     * @param gameName        The name of the game (e.g., "BedWars").
     * @param gameType        The type of the game (e.g., "SkyWars", "CaptureTheFlag").
     * @param baseWorldName   The name of the base/template world to copy.
     * @param teamSizes       The team names and their sizes.
     * @param teamNames       A list of team names (e.g., ["Red", "Blue", "Green"]).
     * @param teamSpawns      A map of team names to spawn locations.
     * @param rawWaitingRoom  A map of the waiting room.
     * @param pluginConfig    A plugin-specific configuration object, or null if not needed.
     * @return The created GameInstance, or null if the creation failed.
     */
    public GameInstance createInstance(
            boolean isInstancePublic,
            String gameName,
            String gameType,
            String baseWorldName,
            Map<String, TeamSize> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Object> pluginConfig
    ) {
        // Extract map-specific data from pluginConfig
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Map<String, Double>>> mapSpecificTeamSpawns =
                (Map<String, Map<String, Map<String, Double>>>) pluginConfig.get("mapSpecificTeamSpawns");

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Double>> mapSpecificWaitingRooms =
                (Map<String, Map<String, Double>>) pluginConfig.get("mapSpecificWaitingRooms");

        // Validate that the map exists in the config
        if (!mapSpecificTeamSpawns.containsKey(baseWorldName) || !mapSpecificWaitingRooms.containsKey(baseWorldName)) {
            plugin.getLogger().warning("No map data found for the selected map: " + baseWorldName);
            return null;
        }

        // Generate the instance UUID
        UUID instanceId = UUID.randomUUID();
        String instanceWorldName = baseWorldName + "_" + instanceId; // Instance-specific world name

        // Resolve spawns and waiting room for the selected map
        Map<String, Map<String, Double>> rawTeamSpawns = mapSpecificTeamSpawns.get(baseWorldName);
        Map<String, Double> rawWaitingRoom = mapSpecificWaitingRooms.get(baseWorldName);

        World instanceWorld = worldManager.createInstanceWorld(baseWorldName, instanceWorldName);
        if (instanceWorld == null) {
            plugin.getLogger().warning("Failed to create instance world: " + instanceWorldName);
            return null;
        }

        Map<String, Location> resolvedTeamSpawns = resolveTeamSpawns(rawTeamSpawns, instanceWorld);
        Location resolvedWaitingRoom = new Location(
                instanceWorld,
                rawWaitingRoom.getOrDefault("x", 0.0),
                rawWaitingRoom.getOrDefault("y", 64.0), // Default Y level
                rawWaitingRoom.getOrDefault("z", 0.0)
        );

        // Create and store the GameInstance
        GameInstance instance = new GameInstance(
                this,
                instanceId, // Pass the generated UUID to the GameInstance
                isInstancePublic,
                gameName,
                gameType,
                instanceWorldName, // Use the unique instance world name
                teamSizes,
                maxTime,
                teamNames,
                resolvedTeamSpawns,
                resolvedWaitingRoom,
                pluginConfig
        );

    activeInstances.put(instanceId, instance);
    startReadinessTimer(instance);
    plugin.getLogger().info("Game instance created successfully: " + instanceWorldName);
    return instance;
}
 


    public List<GameInstance> getInstancesByRegisteredGame(String registeredGameName) {
        return activeInstances.values().stream()
            .filter(instance -> instance.getGameName().equalsIgnoreCase(registeredGameName))
            .toList();
    }

    /**
     * Resolves raw team spawns into actual Location objects using the instance world.
     *
     * @param rawTeamSpawns A map of team names to raw spawn coordinates.
     * @param instanceWorld The world in which to resolve the locations.
     * @return A map of team names to resolved Location objects.
     */
    private Map<String, Location> resolveTeamSpawns(Map<String, Map<String, Double>> rawTeamSpawns, World instanceWorld) {
        Map<String, Location> resolvedSpawns = new HashMap<>();
    
        for (Map.Entry<String, Map<String, Double>> entry : rawTeamSpawns.entrySet()) {
            String teamName = entry.getKey().toLowerCase();
            Map<String, Double> coords = entry.getValue();
    
            if (coords == null || !coords.containsKey("x") || !coords.containsKey("y") || !coords.containsKey("z")) {
                Bukkit.getLogger().warning("Missing or incomplete spawn coordinates for team: " + teamName);
                continue;
            }
    
            Location location = new Location(
                    instanceWorld,
                    coords.getOrDefault("x", 0.0),
                    coords.getOrDefault("y", 64.0), // Default to Y=64
                    coords.getOrDefault("z", 0.0)
            );
    
            resolvedSpawns.put(teamName, location);
        }
    
        return resolvedSpawns;
    }
    


    public GameInstance getInstance(UUID instanceId) {
        return activeInstances.get(instanceId);
    };






    public void startReadinessTimer(GameInstance instance) {
        if (instance.getState() != GameInstance.GameState.WAITING) {
            plugin.getLogger().warning("Cannot start readiness timer: GameInstance is not in the WAITING state.");
            return;
        }
    
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (instance.getState() != GameInstance.GameState.WAITING) {
                Bukkit.getScheduler().cancelTask(instance.getReadinessTaskId());
                instance.clearReadinessTaskId();
                return;
            }
    
            if (instance.getTotalPlayerCount() >= instance.getRequiredPlayers()) {
                Bukkit.getScheduler().cancelTask(instance.getReadinessTaskId());
                instance.startGame(); // Transition the game to IN_PROGRESS
                instance.clearReadinessTaskId();
            } else {
                instance.broadcastToAllPlayers("Waiting for more players: " + 
                    instance.getTotalPlayerCount() + "/" + instance.getRequiredPlayers());
            }
        }, 0L, 20L * 20); // Run every 20 seconds (20 ticks = 1 second)
    
        instance.setReadinessTaskId(taskId); // Track the task ID in the instance
        plugin.getLogger().info("Started readiness timer for GameInstance: " + instance.getInstanceId());
    }
    




    public void closeInstance(UUID instanceId) {
        GameInstance instance = activeInstances.remove(instanceId);
        if (instance == null) {
            plugin.getLogger().warning("Attempted to close an invalid or non-existent instance with ID: " + instanceId);
            return;
        }
    
        // Initialize InventoryManager (replace with actual instance if managed centrally)
        InventoryManager inventoryManager = new InventoryManager(plugin);
    
        // Teleport all players safely to the specified lobby and restore their inventories
        instance.getTeams().values().forEach(playerList -> playerList.forEach(playerUUID -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                // Restore inventory
                inventoryManager.restoreInventory(player);
                player.sendMessage("§aYour inventory has been restored!");
    
                // Teleport to lobby or fallback location
                Location safeLocation = plugin.getLobbyLocation("Spawn");
                if (safeLocation != null) {
                    player.teleport(safeLocation);
                } else {
                    player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                    player.sendMessage("§cUnable to find a safe lobby. Teleported to the world spawn.");
                }
            }
        }));
    
        // End the game if it's still in progress
        if (instance.getState() == GameInstance.GameState.IN_PROGRESS) {
            instance.endGame();
        }
    
        // Unload and delete the instance-specific world
        String instanceWorldName = instance.getWorldName();
        try {
            worldManager.deleteWorld(instanceWorldName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(instanceWorldName + " NOT DELETED!! ");
        }
    
        plugin.getLogger().info("Closed game instance with ID: " + instanceId);
    }
    
    



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
    private void teleportPlayersToSafeLocation(GameInstance instance) {
        String lobbyName = "Spawn";
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
    

    // private void startInstanceTimer(GameInstance instance) {
    //     // Schedule a task to end the game when maxTime expires
    //     Bukkit.getScheduler().runTaskLater(plugin, () -> {
    //         if (instance.getState() == GameInstance.GameState.IN_PROGRESS) {
    //             instance.broadcastToAllPlayers("Time's up! The game has ended.");
    //             instance.endGame();
    //         }
    //     }, instance.getMaxTime() * 20L); // Convert seconds to ticks (20 ticks = 1 second)
    // };

    public void teleportTeamPlayers(GameInstance instance, String team, Player player) {
        Location spawnLocation = instance.getTeamSpawns().get(team);
        if (spawnLocation != null) {
            worldManager.teleportPlayer(player, spawnLocation);
        }
    };
    

    

public void addPlayerToInstance(Player player, GameInstance instance) {
    // Initialize InventoryManager (replace with the actual instance if managed elsewhere)
    InventoryManager inventoryManager = new InventoryManager(plugin);

    // Check if the player is already in a party
    Party playerParty = partyManager.getParty(player);

    // Ensure the party isn't already in another instance
    if (playerParty != null && isPartyInAnotherInstance(playerParty)) {
        player.sendMessage("Your party is already in another game instance.");
        return;
    }

    // Check if the instance is full
    if (instance.isFull()) {
        player.sendMessage("The instance is full. Cannot join.");
        return;
    }

    // Save the player's inventory and clear it
    inventoryManager.saveInventory(player);
    player.sendMessage("§aYour inventory has been safely stored for the game.");

    // Add the player to the waiting room of the instance
    instance.getTeams().computeIfAbsent("waiting_room", k -> new ArrayList<>()).add(player.getUniqueId());

    // Teleport the player to the waiting room
    Location waitingRoomLoc = instance.getWaitingRoom();
    if (waitingRoomLoc != null) {
        player.teleport(waitingRoomLoc);
        player.sendMessage("Welcome to the waiting room! Select your team when you're ready.");
    } else {
        player.sendMessage("Waiting room location is unavailable. Please report this issue.");
        return;
    }

    // Optionally give the team selector book
    boolean canChooseTeams = true; // Later make this a configurable option in games.
    if (canChooseTeams) {
        teamSelectorBook.giveSelectorBook(player, instance.getInstanceId());
    }

    // Notify other players in the waiting room
    instance.broadcastToAllPlayers(player.getName() + " has joined the waiting room!");
}

    
    

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
            String currentTeam = availableTeams.get(teamIndex).toLowerCase();
            instance.getTeams().get(currentTeam).add(memberUUID);
    
            // Check if the current team is now full
            if (instance.getTeams().get(currentTeam).size() >= instance.getTeamSizes().get(currentTeam).getMax()) {
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
            String teamName = entry.getKey().toLowerCase();
            int currentSize = entry.getValue().size();
            int maxSize = instance.getTeamSizes().get(teamName).getMax();
            int minSize = instance.getTeamSizes().get(teamName).getMin();
    
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
    public void assignPlayerToTeam(Player player, GameInstance instance, String teamName) {
        if (!instance.getTeams().containsKey(teamName)) {
            player.sendMessage("Invalid team name. Please choose a valid team.");
            return;
        }
    
        // Remove player from the waiting room
        instance.getTeams().get("waiting_room").remove(player.getUniqueId());
    
        // Check if the team has available slots
        if (instance.getTeams().get(teamName).size() >= instance.getTeamSizes().get(teamName).getMax()) {
            player.sendMessage("The " + teamName + " team is full. Please choose another team.");
            return;
        }
    
        // Add player to the selected team
        instance.getTeams().get(teamName).add(player.getUniqueId());
    
        // Teleport player to the team spawn location
        Location spawnLocation = instance.getTeamSpawns().get(teamName);
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
        } else {
            player.sendMessage("Team spawn location does not exist. Please report this bug.");
        }
    
        instance.broadcastToAllPlayers(player.getName() + " has joined the " + teamName + " team!");
    }
    
    
    
    
    public void handlePlayerLeave(Player player) {
        GameInstance instance = getInstanceForPlayer(player);
        if (instance == null) {
            plugin.getLogger().warning("Player " + player.getName() + " attempted to leave, but no instance was found.");
            return;
        }
    
        // Remove the player from all teams in the instance
        instance.getTeams().values().forEach(team -> team.remove(player.getUniqueId()));
    
        // Restore the player's inventory
        InventoryManager inventoryManager = new InventoryManager(plugin);
        inventoryManager.restoreInventory(player);
        player.sendMessage("§aYour inventory has been restored!");
    
        // Teleport the player to a safe location
        Location safeLocation = plugin.getLobbyLocation("Spawn");
        if (safeLocation != null) {
            player.teleport(safeLocation);
        } else {
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
            player.sendMessage("§cUnable to find a safe lobby. Teleported to the world spawn.");
        }
    
        // Notify others in the instance
        instance.broadcastToAllPlayers(player.getName() + " has left the game.");
    }
    
    
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

    public Map<UUID, GameInstance> getActiveInstances() {
        return Collections.unmodifiableMap(activeInstances);
    }

}

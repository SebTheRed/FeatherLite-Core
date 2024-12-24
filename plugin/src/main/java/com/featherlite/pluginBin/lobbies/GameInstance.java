package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a runtime instance of a minigame session.
 */
public class GameInstance {
    public enum GameState {
        WAITING,
        IN_PROGRESS,
        ENDED
    }


    private final UUID instanceId;
    private final String gameName; // Name of the minigame
    private final String gameType; // Type of the game (e.g., "SkyWars")
    private String worldName; // Name of the world for this instance
    private final Map<String, Integer> teamSizes; // A map of teams and their sizes.
    private final int maxTime; // In minutes
    private final Map<String, List<UUID>> teams; // Maps team names to player UUIDs
    private final Map<String, Location> teamSpawns; // Maps team names to spawn locations
    private final List<UUID> spectators; // List of spectators
    private final Object pluginConfig; // Optional plugin-specific configuration
    private GameState state;

    public GameInstance(
            String gameName,
            String gameType,
            String worldName,
            Map<String, Integer> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Location> teamSpawns,
            Object pluginConfig
    ) {
        this.instanceId = UUID.randomUUID();
        this.gameName = gameName;
        this.gameType = gameType;
        this.worldName = worldName;
        this.teamSizes = teamSizes;
        this.maxTime = maxTime;
        this.teams = new HashMap<>();
        this.teamSpawns = teamSpawns != null ? teamSpawns : new HashMap<>();
        this.spectators = new ArrayList<>();
        this.pluginConfig = pluginConfig;
        this.state = GameState.WAITING;

        // Initialize teams
        for (String team : teamNames) {
            teams.put(team, new ArrayList<>());
        }
    }

    // --- Getters ---
    public UUID getInstanceId() {
        return instanceId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getGameType() {
        return gameType;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean setWorldName(String newName) {
        worldName = newName;
        return true;
    }

    public Map<String, Integer> getTeamSizes() {
        return teamSizes;
    }

    public int getMaxTime() {
        return maxTime;
    }
    
    public Object getPluginConfig() {
        return pluginConfig;
    }

    public Map<String, List<UUID>> getTeams() {
        return teams;
    }

    public Map<String, Location> getTeamSpawns() {
        return teamSpawns;
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    // --- Core Logic ---
    public void startGame() {
        int requiredPlayers = teamSizes.values().stream().mapToInt(Integer::intValue).sum(); // Total required players
    
        if (getTotalPlayerCount() >= requiredPlayers) {
            state = GameState.IN_PROGRESS;
            broadcastToAllPlayers("The game has started!");
        } else {
            broadcastToAllPlayers("Not enough players to start the game.");
        }
    }
    

    public void endGame() {
        state = GameState.ENDED;
        broadcastToAllPlayers("The game has ended!");
    }

    public void addPlayerToTeam(Player player, String teamName) {
        if (isFull()) {
            player.sendMessage("This game is full.");
            return;
        }

        String targetTeam = teamName != null && teams.containsKey(teamName) ? teamName : getRandomAvailableTeam();

        if (targetTeam == null) {
            player.sendMessage("No available teams found.");
            return;
        }

        teams.get(targetTeam).add(player.getUniqueId());
        player.teleport(teamSpawns.get(targetTeam));
        broadcastToAllPlayers(player.getName() + " joined the " + targetTeam + " team.");
    }

    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.sendMessage("You are now spectating this game.");
    }

    public boolean isFull() {
        return teams.entrySet().stream()
                .allMatch(entry -> entry.getValue().size() >= teamSizes.get(entry.getKey())); // All teams must be full
    }
    

    public int getTotalPlayerCount() {
        return teams.values().stream().mapToInt(List::size).sum();
    }

    private String getRandomAvailableTeam() {
        return teams.entrySet().stream()
                .filter(entry -> entry.getValue().size() < teamSizes.get(entry.getKey())) // Check against the team's size limit
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(null);
    }
    

    public void broadcastToAllPlayers(String message) {
        teams.values().forEach(players ->
                players.forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) player.sendMessage(message);
                })
        );
    }
}

package com.featherlite.pluginBin.lobbies;

import com.featherlite.pluginBin.lobbies.InstanceManager;
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

    private final InstanceManager instanceManager;
    private final UUID instanceId;
    private final boolean isInstancePublic;
    private final String gameName; // Name of the minigame
    private final String gameType; // Type of the game (e.g., "Bedwars")
    private String worldName; // Name of the world for this instance
    private final Map<String, Map<String, Integer>> teamSizes; // A map of teams and their sizes.
    private final int maxTime; // In minutes
    private final Map<String, List<UUID>> teams; // Maps team names to player UUIDs
    private final Map<String, Location> teamSpawns; // Maps team names to spawn locations
    private Location waitingRoom;
    private final List<UUID> spectators; // List of spectators
    private final Object pluginConfig; // Optional plugin-specific configuration
    private GameState state;

    private int readinessTaskId = -1; // To track the scheduled task ID

    public GameInstance(
            InstanceManager instanceManager,
            boolean isInstancePublic,
            String gameName,
            String gameType,
            String worldName,
            Map<String, Map<String, Integer>> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Location> teamSpawns,
            Location waitingRoom,
            Object pluginConfig
    ) {
        this.instanceManager = instanceManager;
        this.instanceId = UUID.randomUUID();
        this.isInstancePublic = isInstancePublic;
        this.gameName = gameName;
        this.gameType = gameType;
        this.worldName = worldName;
        this.teamSizes = teamSizes;
        this.maxTime = maxTime;
        this.teams = new HashMap<>();
        this.teamSpawns = teamSpawns != null ? teamSpawns : new HashMap<>();
        this.waitingRoom = waitingRoom;
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

    public boolean getIsInstancePublic() {
        return isInstancePublic;
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

    public Map<String, Map<String, Integer>> getTeamSizes() {
        return teamSizes;
    }

    // Getter for the number of required players
    public int getRequiredPlayers() {
        return teamSizes.values().stream()
                .mapToInt(sizeMap -> sizeMap.getOrDefault("min", 0))
                .sum();
    }

    // Getter and Setter for the readiness task ID
    public int getReadinessTaskId() {
        return readinessTaskId;
    }

    public void setReadinessTaskId(int taskId) {
        this.readinessTaskId = taskId;
    }

    public void clearReadinessTaskId() {
        this.readinessTaskId = -1;
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

    public Location getWaitingRoom() {
        return waitingRoom;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    // --- Core Logic ---
    public void startGame() {
        if (state != GameState.WAITING) return;
    
        state = GameState.IN_PROGRESS;
    
        // Distribute players into teams
        assignPlayersToTeams();
    
        // Teleport players to their respective team spawns
        teleportPlayersToTeamSpawns();
    
        // Notify players
        broadcastToAllPlayers("The game has started! Good luck!");
    }
    
    

    public void endGame() {
        state = GameState.ENDED;
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this));
        broadcastToAllPlayers("The game has ended!");
        instanceManager.closeInstance(instanceId);

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
    
        if (teams.get(targetTeam).size() >= teamSizes.get(targetTeam).getOrDefault("max", Integer.MAX_VALUE)) {
            player.sendMessage("The " + targetTeam + " team is full.");
            return;
        }
    
        teams.get(targetTeam).add(player.getUniqueId());
        player.teleport(teamSpawns.get(targetTeam));
        broadcastToAllPlayers(player.getName() + " joined the " + targetTeam + " team.");
    }
    
    
    public void teleportPlayersToWaitingRoom() {
        teams.values().forEach(players ->
                players.forEach(playerUUID -> {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null && player.isOnline()) {
                        player.teleport(waitingRoom);
                        player.sendMessage("You have been teleported to the waiting room. Please wait for the game to start.");
                    }
                })
        );
    }

    // Assign players to teams as evenly as possible
    private void assignPlayersToTeams() {
        List<Player> waitingPlayers = getAllPlayersInWaitingRoom();
        List<String> teamNames = new ArrayList<>(teams.keySet());

        int teamIndex = 0;
        for (Player player : waitingPlayers) {
            String teamName = teamNames.get(teamIndex);
            teams.get(teamName).add(player.getUniqueId());
            teamIndex = (teamIndex + 1) % teamNames.size(); // Rotate to the next team
        }
    }

    // Teleport players to their assigned team spawns
    private void teleportPlayersToTeamSpawns() {
        teams.forEach((teamName, playerList) -> {
            Location spawnLocation = teamSpawns.get(teamName);
            for (UUID playerId : playerList) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.teleport(spawnLocation);
                    player.sendMessage("You have been teleported to the " + teamName + " spawn!");
                }
            }
        });
    }


    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
        player.sendMessage("You are now spectating this game.");
    }

    public boolean isFull() {
        return teams.entrySet().stream()
                .allMatch(entry -> entry.getValue().size() >= teamSizes.get(entry.getKey()).getOrDefault("max", Integer.MAX_VALUE));
    }
    

    public int getTotalPlayerCount() {
        return teams.values().stream().mapToInt(List::size).sum();
    }

    private String getRandomAvailableTeam() {
        return teams.entrySet().stream()
                .filter(entry -> entry.getValue().size() < teamSizes.get(entry.getKey()).getOrDefault("max", Integer.MAX_VALUE))
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


    private List<Player> getAllPlayersInWaitingRoom() {
        return teams.values().stream()
                .flatMap(List::stream)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }

}

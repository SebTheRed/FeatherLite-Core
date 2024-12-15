package com.featherlite.pluginBin.lobbies;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class GameInstance {
    public enum GameState {
        WAITING,
        IN_PROGRESS,
        ENDED
    }

    private final UUID instanceId;
    private final String gameType;
    private final int minPlayers;
    private final int maxPlayers;
    private final int maxTime;
    private final Map<String, List<UUID>> teams; // Maps team name to player UUIDs
    private final Map<String,Location> teamSpawns;
    private final Map<UUID, String> partyToTeam; // Maps party leader UUID to their team
    private GameState state;
    private final List<UUID> spectators;
    private String worldName;

    public GameInstance(String gameType, int minPlayers, int maxPlayers, List<String> teamNames, int maxTime) {
        this.instanceId = UUID.randomUUID();
        this.gameType = gameType;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.maxTime = maxTime;
        this.teams = new HashMap<>();
        this.teamSpawns = new HashMap<>();
        this.partyToTeam = new HashMap<>();
        this.state = GameState.WAITING;
        this.spectators = new ArrayList<>();

        // Initialize empty teams based on the team names
        for (String team : teamNames) {
            teams.put(team, new ArrayList<>());
        }
    }

    public UUID getInstanceId() {
        return instanceId;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getGameType() {
        return gameType;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Map<String, List<UUID>> getTeams() {
        return teams;
    }

        public void setTeamSpawns(Map<String, Location> spawns) {
        this.teamSpawns.putAll(spawns);
    }

    public Map<String, Location> getTeamSpawns() {
        return teamSpawns;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    // Overfill method to add a party to a team, allowing members to spread across teams if needed
    public void addPartyToTeam(Party party) {
        List<UUID> remainingMembers = new ArrayList<>(party.getMembers());
        List<String> assignedTeams = new ArrayList<>();
        int currentPlayerCount = teams.values().stream().mapToInt(List::size).sum();

        // Calculate available slots
        int availableSlots = maxPlayers - currentPlayerCount;
        if (remainingMembers.size() > availableSlots) {
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader != null) {
                leader.sendMessage("There isn't enough space in the game for your entire party. Please try again with fewer members.");
            }
            return;
        }

        // Attempt to assign each member to a team with available space
        for (String teamName : teams.keySet()) {
            List<UUID> teamMembers = teams.get(teamName);

            // Fill the team until we hit the per-team max or we run out of party members
            while (!remainingMembers.isEmpty() && teamMembers.size() < maxPlayers / teams.size()) {
                UUID member = remainingMembers.remove(0);
                teamMembers.add(member);
                assignedTeams.add(teamName);
            }

            if (remainingMembers.isEmpty()) break;
        }

        // If there are remaining members, continue assigning even if teams are "overfilled"
        if (!remainingMembers.isEmpty()) {
            for (String teamName : teams.keySet()) {
                List<UUID> teamMembers = teams.get(teamName);

                while (!remainingMembers.isEmpty() && currentPlayerCount < maxPlayers) {
                    UUID member = remainingMembers.remove(0);
                    teamMembers.add(member);
                    assignedTeams.add(teamName);
                    currentPlayerCount++;
                }

                if (remainingMembers.isEmpty()) break;
            }
        }

        // Assign the party leader's team for tracking purposes
        if (!assignedTeams.isEmpty()) {
            partyToTeam.put(party.getLeader(), assignedTeams.get(0));
        }

        // Notify the party if all members were assigned to teams
        if (!remainingMembers.isEmpty()) {
            Player leader = Bukkit.getPlayer(party.getLeader());
            if (leader != null) {
                leader.sendMessage("Some members of your party couldn't be assigned to the same team due to game limits.");
            }
        } else {
            broadcastToPlayers(party.getMembers(), "Your party has been added to the teams: " + String.join(", ", assignedTeams));
        }
    }
    
    public void addSpectator(Player player) {
        spectators.add(player.getUniqueId());
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public boolean isFull() {
        return teams.values().stream().mapToInt(List::size).sum() >= maxPlayers;
    }

    public boolean hasMinPlayers() {
        return teams.values().stream().mapToInt(List::size).sum() >= minPlayers;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public void startGame() {
        if (hasMinPlayers() && state == GameState.WAITING) {
            state = GameState.IN_PROGRESS;
            broadcastToAllPlayers("The game has started!");
        }
    }

    public void endGame() {
        if (state == GameState.IN_PROGRESS) {
            state = GameState.ENDED;
            broadcastToAllPlayers("The game has ended!");
        }
    }

    
    public void broadcastToAllPlayers(String message) {
        teams.values().forEach(playerList -> playerList.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        }));
    }

    public void broadcastToPlayers(Collection<UUID> players, String message) {
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.sendMessage(message);
            }
        });
    }
}

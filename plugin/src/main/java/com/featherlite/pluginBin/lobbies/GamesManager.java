package com.featherlite.pluginBin.lobbies;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages the registration and data storage for all available games.
 */
public class GamesManager {

    /**
     * Represents the metadata and configuration for a registered game.
     */
    public static class GameData {
        private final String gameName;
        private final String gameType;
        private final String description;
        private final List<String> worldOptions;
        private final Map<String, Integer> teamSizes;
        private final int maxTime;
        private final List<String> teamNames;
        private final Map<String, Object> pluginConfig;

        public GameData(
                String gameName,
                String gameType,
                String description,
                List<String> worldOptions,
                Map<String, Integer> teamSizes,
                int maxTime,
                List<String> teamNames,
                Map<String, Object> pluginConfig
        ) {
            this.gameName = gameName;
            this.gameType = gameType;
            this.description = description;
            this.worldOptions = worldOptions;
            this.teamSizes = teamSizes;
            this.maxTime = maxTime;
            this.teamNames = teamNames;
            this.pluginConfig = pluginConfig;
        }

        public String getGameName() {
            return gameName;
        }

        public String getGameType() {
            return gameType;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getWorldOptions() {
            return worldOptions;
        }

        public Map<String, Integer> getTeamSizes() {
            return teamSizes;
        }

        public int getMaxTime() {
            return maxTime;
        }

        public List<String> getTeamNames() {
            return teamNames;
        }

        public Map<String, Object> getPluginConfig() {
            return pluginConfig;
        }

    }

    private final Map<String, GameData> registeredGames = new HashMap<>();
    private final Map<String, List<GameData>> gamesByType = new HashMap<>(); // Group games by type


    /**
     * Registers a new game with the GamesManager.
     *
     * @param gameName      The name of the game (e.g. Bedwars-Quads).
     * @param gameType      The type of the game (e.g., "Bedwars").
     * @param description   A brief description of the game.
     * @param worldOptions  The names of the world choices for the game.
     * @param teamSizes    The max-sizes of teams.
     * @param maxTime       The maximum time (in seconds) for the game.
     * @param teamNames     The names of the teams.
     * @param pluginConfig  Plugin-specific configuration, or null if not needed.
     */
    public void registerGame(
            String gameName,
            String gameType,
            String description,
            List<String> worldOptions,
            Map<String,Integer> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Object> pluginConfig
    ) {
        if (registeredGames.containsKey(gameName)) {
            throw new IllegalArgumentException("A game with this name is already registered: " + gameName);
        }

        GameData gameData = new GameData(
            gameName,
            gameType,
            description,
            worldOptions,
            teamSizes,
            maxTime,
            teamNames,
            pluginConfig // Store all map-specific data in pluginConfig
        );

        registeredGames.put(gameName, gameData);
        gamesByType.computeIfAbsent(gameType, k -> new ArrayList<>()).add(gameData);

    }

    /**
     * Retrieves the data for a registered game by its name.
     *
     * @param gameName The name of the game.
     * @return The GameData for the game, or null if not found.
     */
    public GameData getGameData(String gameName) {
        return registeredGames.get(gameName);
    }

    /**
     * Retrieves all game types.
     *
     * @return A set of all game types.
     */
    public Set<String> getAllGameTypes() {
        return gamesByType.keySet();
    }

    /**
     * Retrieves all games of a specific type.
     *
     * @param gameType The type of the game (case-insensitive).
     * @return A list of all games of the specified type, or an empty list if none exist.
     */
    public List<GameData> getGamesByType(String gameType) {
        return gamesByType.getOrDefault(gameType, Collections.emptyList());
    }
    /**
     * Starts a new game instance using the registered game data.
     *
     * @param gameName        The name of the game to start.
     * @param instanceManager The InstanceManager to handle the game instance.
     * @return The created GameInstance, or null if the game could not be started.
     */
    public GameInstance startGameInstance(String gameName, String worldChoice, boolean isInstancePublic, InstanceManager instanceManager) {
        GameData gameData = getGameData(gameName);
        if (gameData == null) {
            throw new IllegalArgumentException("No game registered with the name: " + gameName);
        }

        return instanceManager.createInstance(
                isInstancePublic,
                gameData.getGameName(),
                gameData.getGameType(),
                worldChoice,
                gameData.getTeamSizes(),
                gameData.getMaxTime(),
                gameData.getTeamNames(),
                gameData.getPluginConfig()

        );
    }

    /**
     * Lists all registered games.
     *
     * @return A collection of all registered GameData objects.
     */
    public Collection<GameData> listRegisteredGames() {
        return registeredGames.values();
    }
}

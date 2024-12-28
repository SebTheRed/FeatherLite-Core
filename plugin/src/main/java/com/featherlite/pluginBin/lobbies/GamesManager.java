package com.featherlite.pluginBin.lobbies;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

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
        private final Plugin ownerPlugin;

        public GameData(
                String gameName,
                String gameType,
                String description,
                List<String> worldOptions,
                Map<String, Integer> teamSizes,
                int maxTime,
                List<String> teamNames,
                Map<String, Object> pluginConfig,
                Plugin ownerPlugin
        ) {
            this.gameName = gameName;
            this.gameType = gameType;
            this.description = description;
            this.worldOptions = worldOptions;
            this.teamSizes = teamSizes;
            this.maxTime = maxTime;
            this.teamNames = teamNames;
            this.pluginConfig = pluginConfig;
            this.ownerPlugin = ownerPlugin;
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

        public Plugin getOwnerPlugin() {
            return ownerPlugin;
        }
    }

    private final Map<String, GameData> registeredGames = new HashMap<>();

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
     * @param ownerPlugin   The plugin that owns the game.
     */
    public void registerGame(
            String gameName,
            String gameType,
            String description,
            List<String> worldOptions,
            Map<String,Integer> teamSizes,
            int maxTime,
            List<String> teamNames,
            Map<String, Object> pluginConfig,
            Plugin ownerPlugin
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
            pluginConfig, // Store all map-specific data in pluginConfig
            ownerPlugin
        );

        registeredGames.put(gameName, gameData);
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
     * Starts a new game instance using the registered game data.
     *
     * @param gameName        The name of the game to start.
     * @param instanceManager The InstanceManager to handle the game instance.
     * @return The created GameInstance, or null if the game could not be started.
     */
    public GameInstance startGameInstance(String gameName, String worldChoice, InstanceManager instanceManager) {
        GameData gameData = getGameData(gameName);
        if (gameData == null) {
            throw new IllegalArgumentException("No game registered with the name: " + gameName);
        }

        return instanceManager.createInstance(
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

package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class GameConfiguration {

    private final JavaPlugin plugin;
    private final Map<String, Map<String, GameConfig>> gameCategories = new HashMap<>();

    public GameConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigurationsFromAllPlugins();
    }

    // Load configurations from all plugins
    public void loadConfigurationsFromAllPlugins() {
        gameCategories.clear();

        loadConfigurationFromPlugin(plugin, "FeatherLite-Core");

        for (Plugin installedPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (installedPlugin.isEnabled() && !installedPlugin.equals(plugin)) {
                loadConfigurationFromPlugin(installedPlugin, installedPlugin.getName());
            }
        }
    }

    private void loadConfigurationFromPlugin(Plugin plugin, String pluginName) {
        File configFile = new File(plugin.getDataFolder(), "game-rules.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("No game-rules.yml found for " + pluginName);
            return;
        }

        plugin.getLogger().info("Loading game-rules.yml from " + pluginName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        parseConfiguration(config);
    }

    private void parseConfiguration(FileConfiguration config) {
        ConfigurationSection categoriesSection = config.getConfigurationSection("gameCategories");
        if (categoriesSection == null) return;

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection category = categoriesSection.getConfigurationSection(categoryName);
            if (category == null) continue;

            Map<String, GameConfig> gameModes = new HashMap<>();

            for (String modeName : category.getKeys(false)) {
                ConfigurationSection modeSection = category.getConfigurationSection(modeName);

                if (modeSection == null) continue;

                int minPlayers = modeSection.getInt("minPlayers", 2);
                int maxPlayers = modeSection.getInt("maxPlayers", 16);
                int minTeams = modeSection.getInt("minTeams", 2);
                int maxTeams = modeSection.getInt("maxTeams", 4);
                List<String> teams = modeSection.getStringList("teams");
                int maxTime = modeSection.getInt("maxTime", 300);

                Map<String, Map<String, Location>> worldSpawns = new HashMap<>();
                Map<String, List<GeneratorConfig>> worldGenerators = new HashMap<>();

                ConfigurationSection worldsSection = modeSection.getConfigurationSection("worlds");
                if (worldsSection != null) {
                    for (String worldName : worldsSection.getKeys(false)) {
                        ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
                        if (worldSection == null) continue;

                        Map<String, Location> teamSpawns = new HashMap<>();
                        ConfigurationSection spawnsSection = worldSection.getConfigurationSection("spawns");
                        if (spawnsSection != null) {
                            for (String teamName : spawnsSection.getKeys(false)) {
                                ConfigurationSection spawn = spawnsSection.getConfigurationSection(teamName);
                                if (spawn != null) {
                                    teamSpawns.put(teamName, parseLocation(worldName, spawn));
                                }
                            }
                        }

                        List<GeneratorConfig> generators = new ArrayList<>();
                        ConfigurationSection generatorsSection = worldSection.getConfigurationSection("generators");
                        if (generatorsSection != null) {
                            for (String generatorName : generatorsSection.getKeys(false)) {
                                ConfigurationSection generator = generatorsSection.getConfigurationSection(generatorName);
                                if (generator != null) {
                                    List<String> drops = generator.getStringList("drops");
                                    generators.add(new GeneratorConfig(generatorName, parseLocation(worldName, generator), drops));
                                }
                            }
                        }

                        worldSpawns.put(worldName, teamSpawns);
                        worldGenerators.put(worldName, generators);
                    }
                }

                gameModes.put(modeName, new GameConfig(categoryName, modeName, minPlayers, maxPlayers, minTeams, maxTeams, teams, maxTime, worldSpawns, worldGenerators));
            }

            gameCategories.put(categoryName, gameModes);
        }
    }

    private Location parseLocation(String worldName, ConfigurationSection section) {
        double x = section.getDouble("x", 0);
        double y = section.getDouble("y", 64);
        double z = section.getDouble("z", 0);
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public GameConfig getGameMode(String category, String mode) {
        return gameCategories.getOrDefault(category, Collections.emptyMap()).get(mode);
    }

    public static class GeneratorConfig {
        private final String name;
        private final Location location;
        private final List<String> drops;

        public GeneratorConfig(String name, Location location, List<String> drops) {
            this.name = name;
            this.location = location;
            this.drops = drops;
        }

        public String getName() { return name; }
        public Location getLocation() { return location; }
        public List<String> getDrops() { return drops; }
    }

    public static class GameConfig {
        private final String category;
        private final String modeName;
        private final int minPlayers, maxPlayers, minTeams, maxTeams, maxTime;
        private final List<String> teams;
        private final Map<String, Map<String, Location>> worldSpawns;
        private final Map<String, List<GeneratorConfig>> worldGenerators;
    
        public GameConfig(
                String category,
                String modeName,
                int minPlayers,
                int maxPlayers,
                int minTeams,
                int maxTeams,
                List<String> teams,
                int maxTime,
                Map<String, Map<String, Location>> worldSpawns,
                Map<String, List<GeneratorConfig>> worldGenerators
        ) {
            this.category = category;
            this.modeName = modeName;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
            this.minTeams = minTeams;
            this.maxTeams = maxTeams;
            this.teams = teams;
            this.maxTime = maxTime;
            this.worldSpawns = worldSpawns;
            this.worldGenerators = worldGenerators;
        }
    
        // Getters for all fields
    
        /**
         * Get the category of the game (e.g., "bedwars").
         */
        public String getCategory() {
            return category;
        }
    
        /**
         * Get the mode name of the game (e.g., "fourBed").
         */
        public String getModeName() {
            return modeName;
        }
    
        /**
         * Get the minimum number of players required to start the game.
         */
        public int getMinPlayers() {
            return minPlayers;
        }
    
        /**
         * Get the maximum number of players allowed in the game.
         */
        public int getMaxPlayers() {
            return maxPlayers;
        }
    
        /**
         * Get the minimum number of teams required to start the game.
         */
        public int getMinTeams() {
            return minTeams;
        }
    
        /**
         * Get the maximum number of teams allowed in the game.
         */
        public int getMaxTeams() {
            return maxTeams;
        }
    
        /**
         * Get the list of team names (e.g., ["Red", "Blue", "Green", "Yellow"]).
         */
        public List<String> getTeams() {
            return teams;
        }
    
        /**
         * Get the maximum time (in seconds) for the game to run.
         */
        public int getMaxTime() {
            return maxTime;
        }
    
        /**
         * Get the map of world spawns, organized by world name and team name.
         */
        public Map<String, Map<String, Location>> getWorldSpawns() {
            return worldSpawns;
        }
    
        /**
         * Get the list of generator configurations for a specific world.
         * @param worldName The name of the world to retrieve generators for.
         */
        public List<GeneratorConfig> getGeneratorsForWorld(String worldName) {
            return worldGenerators.getOrDefault(worldName, Collections.emptyList());
        }
    
        /**
         * Get all generator configurations across all worlds.
         */
        public Map<String, List<GeneratorConfig>> getWorldGenerators() {
            return worldGenerators;
        }
    
        /**
         * Get the spawn location for a specific team in a specific world.
         * @param worldName The name of the world.
         * @param teamName The name of the team.
         */
        public Location getSpawnForTeam(String worldName, String teamName) {
            Map<String, Location> spawns = worldSpawns.get(worldName);
            return spawns != null ? spawns.get(teamName) : null;
        }
    }
}

package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;

import java.io.File;
import java.util.*;

public class GameConfiguration {
    private final JavaPlugin plugin;
    private final Map<String, Map<String, GameConfig>> gameCategories = new HashMap<>();

    public GameConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigurationsFromAllPlugins();
    };

    // Loads configuration from all detected mini-game plugins
    public void loadConfigurationsFromAllPlugins() {
        // Clear previous configurations to avoid duplicates
        gameCategories.clear();

        // Load Core plugin configurations (as a fallback or core setup)
        loadConfigurationFromPlugin(plugin, "FeatherLite-Core");

        // Attempt to load configurations from all installed plugins
        for (Plugin installedPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (installedPlugin.isEnabled() && !installedPlugin.equals(plugin)) {
                loadConfigurationFromPlugin(installedPlugin, installedPlugin.getName());
            }
        }
    };

    // Load configuration from a specific plugin
    private void loadConfigurationFromPlugin(Plugin plugin, String pluginName) {
        File configFile = new File(plugin.getDataFolder(), "game-rules.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("No game-rules.yml found for " + pluginName);
            return;
        }

        plugin.getLogger().info("Loading game-rules.yml from " + pluginName);
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        parseConfiguration(config);
    };

    private void parseConfiguration(FileConfiguration config) {
        if (!config.contains("gameCategories")) return;
    
        Map<String, Object> categories = config.getConfigurationSection("gameCategories").getValues(false);
    
        for (String categoryName : categories.keySet()) {
            Map<String, GameConfig> gameModes = gameCategories.getOrDefault(categoryName, new HashMap<>());
            Map<String, Object> categoryDetails = config.getConfigurationSection("gameCategories." + categoryName).getValues(false);
    
            for (String modeName : categoryDetails.keySet()) {
                String basePath = "gameCategories." + categoryName + "." + modeName;
    
                int minPlayers = config.getInt(basePath + ".minPlayers");
                int maxPlayers = config.getInt(basePath + ".maxPlayers");
                int minTeams = config.getInt(basePath + ".minTeams");
                int maxTeams = config.getInt(basePath + ".maxTeams");
                List<String> teams = config.getStringList(basePath + ".teams");
                int maxTime = config.getInt(basePath + ".maxTime");
    
                Map<String, Map<String, Location>> worldSpawns = new HashMap<>();
                if (config.contains(basePath + ".worlds")) {
                    Set<String> worldKeys = config.getConfigurationSection(basePath + ".worlds").getKeys(false);
                    for (String worldName : worldKeys) {
                        Map<String, Location> teamSpawns = new HashMap<>();
                        for (String teamName : teams) {
                            if (config.contains(basePath + ".worlds." + worldName + ".spawns." + teamName)) {
                                String spawnPath = basePath + ".worlds." + worldName + ".spawns." + teamName;
                                double x = config.getDouble(spawnPath + ".x");
                                double y = config.getDouble(spawnPath + ".y");
                                double z = config.getDouble(spawnPath + ".z");
                                teamSpawns.put(teamName, new Location(Bukkit.getWorld(worldName), x, y, z));
                            }
                        }
                        worldSpawns.put(worldName, teamSpawns);
                    }
                }
    
                gameModes.put(modeName, new GameConfig(categoryName, modeName, minPlayers, maxPlayers, minTeams, maxTeams, teams, maxTime, worldSpawns));
            }
    
            gameCategories.put(categoryName, gameModes);
        }
    };

    public GameConfig getGameMode(String category, String mode) {
        return gameCategories.getOrDefault(category, Collections.emptyMap()).get(mode);
    };

    public static class GameConfig {
        private final String category;
        private final String modeName;
        private final int minPlayers;
        private final int maxPlayers;
        private final int minTeams;
        private final int maxTeams;
        private final List<String> teams;
        private final int maxTime;
        private final Map<String, Map<String, Location>> worldSpawns; // Holds world names, team names, and their respective locations
    
        public GameConfig(String category, String modeName, int minPlayers, int maxPlayers, int minTeams, int maxTeams, List<String> teams, int maxTime, Map<String, Map<String, Location>> worldSpawns) {
            this.category = category;
            this.modeName = modeName;
            this.minPlayers = minPlayers;
            this.maxPlayers = maxPlayers;
            this.minTeams = minTeams;
            this.maxTeams = maxTeams;
            this.teams = teams;
            this.maxTime = maxTime;
            this.worldSpawns = worldSpawns;
        }
    
        // Getter methods
        public String getCategory() { return category; }
        public String getModeName() { return modeName; }
        public int getMinPlayers() { return minPlayers; }
        public int getMaxPlayers() { return maxPlayers; }
        public int getMinTeams() { return minTeams; }
        public int getMaxTeams() { return maxTeams; }
        public List<String> getTeams() { return teams; }
        public int getMaxTime() { return maxTime; }
        public Map<String, Map<String, Location>> getWorldSpawns() { return worldSpawns; }
    };
}

package com.featherlite.pluginBin.scoreboards;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ScoreboardParser {
    private final Plugin plugin;
    private final boolean isDebuggerOn;

    public ScoreboardParser(Plugin plugin, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.isDebuggerOn = isDebuggerOn;
    }

    public Map<String, ScoreboardDisplay> parseScoreboards() {
        Map<String, ScoreboardDisplay> scoreboards = new HashMap<>();

        File scoreboardFolder = new File(plugin.getDataFolder(), "scoreboards");
        if (!scoreboardFolder.exists()) {
            plugin.getLogger().warning("No scoreboards folder found for " + plugin.getName());
            return scoreboards;
        }

        for (File file : scoreboardFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                // Parse the scoreboard
                String name = file.getName().replace(".yml", "");
                ScoreboardDisplay display = new ScoreboardDisplay(name, config, plugin, isDebuggerOn);
                scoreboards.put(name, display);

                plugin.getLogger().info("Loaded scoreboard: " + name);
            }
        }

        return scoreboards;
    }
}

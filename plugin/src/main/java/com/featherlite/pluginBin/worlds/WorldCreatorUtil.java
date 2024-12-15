package com.featherlite.pluginBin.worlds;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldCreatorUtil {
    private final JavaPlugin plugin;

    public WorldCreatorUtil(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public World createNewWorld(String worldName, World.Environment environment, WorldType type) {
        WorldCreator creator = new WorldCreator(worldName)
                .environment(environment)
                .type(type);
        return creator.createWorld();
    }
}
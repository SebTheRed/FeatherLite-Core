package com.featherlite.pluginBin.items;

import com.featherlite.pluginBin.FeatherCore;
import com.featherlite.pluginBin.displays.DisplayPieceManager;
import com.featherlite.pluginBin.particles.ParticleManager;
import com.featherlite.pluginBin.projectiles.ProjectileManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;


public class AbilityRegistry {
    private final JavaPlugin plugin;
    private final Map<String, AbilityInfo> abilityRegistry = new HashMap<>();

    private final ProjectileManager projectileManager;
    private final ParticleManager particleManager;
    private final DisplayPieceManager displayPieceManager;
    private final boolean isDebuggerOn;

    public AbilityRegistry(JavaPlugin plugin, ProjectileManager projectileManager, ParticleManager particleManager, DisplayPieceManager displayPieceManager, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.projectileManager = projectileManager;
        this.particleManager = particleManager;
        this.displayPieceManager = displayPieceManager;
        this.isDebuggerOn = isDebuggerOn;
        loadAbilitiesFromPlugins();
    }

    /**
     * Loads abilities from all plugins, including `FeatherCore`.
     */
    public void loadAbilitiesFromPlugins() {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            loadAbilitiesFromPlugin(plugin);
        }
    }

    /**
     * Loads abilities from a specific plugin's `config.yml` under `item-abilities`.
     * For `FeatherCore`, it links to `InternalAbilities`.
     *
     * @param plugin The plugin to load abilities from.
     */
    public void loadAbilitiesFromPlugin(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection abilitiesSection = config.getConfigurationSection("item-abilities");
    
        if (abilitiesSection == null) {
            plugin.getLogger().severe("No `itemAbilities` section found in config.yml for " + plugin.getName());
            return;
        }
    
        for (String abilityName : abilitiesSection.getKeys(false)) {
            ConfigurationSection abilityConfig = abilitiesSection.getConfigurationSection(abilityName);
            if (abilityConfig == null) continue;
    
            // Get the class and method name from the config
            String className = abilityConfig.getString("class");
            String methodName = abilityConfig.getString("method");
    
            if (methodName == null || methodName.isEmpty()) {
                plugin.getLogger().warning("Ability " + abilityName + " in " + plugin.getName() + " is missing a 'method' field.");
                continue;
            }
    
            Map<String, String> params = new HashMap<>();
            ConfigurationSection paramsSection = abilityConfig.getConfigurationSection("params");
            if (paramsSection != null) {
                for (String paramKey : paramsSection.getKeys(false)) {
                    String paramValue = paramsSection.getString(paramKey);
                    if (paramValue != null) {
                        params.put(paramKey, paramValue);
                    }
                }
            }
    
            // Dynamically load the class based on `className`
            Object targetInstance;
            try {
                if (className != null) {
                    Class<?> abilityClass = Class.forName(className);
                    targetInstance = abilityClass.getConstructor().newInstance();
                } else if (plugin instanceof FeatherCore) {
                    targetInstance = new InternalAbilities(projectileManager, particleManager, displayPieceManager, isDebuggerOn); // Use InternalAbilities for FeatherCore abilities
                } else {
                    targetInstance = plugin;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error loading class " + className + " for ability " + abilityName + ": " + e.getMessage());
                continue;
            }
    
            // Register the ability in the registry
            AbilityInfo abilityInfo = new AbilityInfo(targetInstance, methodName, params);
            abilityRegistry.put(abilityName, abilityInfo);
            if (isDebuggerOn) {plugin.getLogger().info("Registered ability: " + abilityName + " from " + plugin.getName() + " with method: " + methodName);}
        }
    }
    

    /**
     * Invokes an ability for a player by its name.
     */
    public boolean invokeAbility(Player player, String abilityName, Map<String, String> params) {
        AbilityInfo abilityInfo = abilityRegistry.get(abilityName);
        if (abilityInfo == null) {
            player.sendMessage("§cAbility not found: " + abilityName);
            plugin.getLogger().warning("Ability '" + abilityName + "' not found in the registry.");
            return false;
        }
    
        Map<String, String> finalParams = new HashMap<>(abilityInfo.getDefaultParams());
        if (params != null) {
            finalParams.putAll(params);
        }
    
        try {
            // Log the class and method that we’re trying to invoke
            Object abilitySource = abilityInfo.getAbilitySource();
            String methodName = abilityInfo.getMethodName();
            if (isDebuggerOn) {plugin.getLogger().info("Attempting to invoke method '" + methodName + "' on class '" + abilitySource.getClass().getName() + "'");}
    
            // Check if method exists with expected parameters (Player and Map)
            Method method = abilitySource.getClass().getMethod(methodName, Player.class, Map.class);
    
            // Log parameters being passed to the method for verification
            if (isDebuggerOn) {plugin.getLogger().info("Parameters for ability '" + abilityName + "': " + finalParams);}
    
            // Invoke the method
            player.sendMessage("§aExecuting ability: " + abilityName + " from " + abilityInfo.getSourceName());
            method.invoke(abilitySource, player, finalParams);
            player.sendMessage("§aExecuted ability: " + abilityName + " from " + abilityInfo.getSourceName());
            
            return true;
    
        } catch (NoSuchMethodException e) {
            player.sendMessage("§cAbility method not found: " + abilityInfo.getMethodName());
            plugin.getLogger().warning("NoSuchMethodException: The method '" + abilityInfo.getMethodName() + "' was not found on class '" + abilityInfo.getAbilitySource().getClass().getName() + "'.");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cError executing ability: Incorrect parameters.");
            plugin.getLogger().warning("IllegalArgumentException: Parameter mismatch when invoking '" + abilityInfo.getMethodName() + "' for ability '" + abilityName + "'. Expected (Player, Map<String, String>).");
        } catch (Exception e) {
            player.sendMessage("§cError executing ability: " + abilityName);
            plugin.getLogger().warning("Exception executing ability: " + abilityName);
            e.printStackTrace();
        }
        return false;
    }
    


    public boolean invokeBlockAbility(Player player, String abilityName, Location baseLocation, Map<String, String> params) {
        AbilityInfo abilityInfo = abilityRegistry.get(abilityName);
        if (abilityInfo == null) {
            player.sendMessage("§cAbility not found: " + abilityName);
            return false;
        }
    
        try {
            // Get the method that takes Player, Location, and Map as parameters
            Method method = abilityInfo.getAbilitySource().getClass()
                .getMethod(abilityInfo.getMethodName(), Player.class, Location.class, Map.class);
            
            // Invoke the method with player, baseLocation, and params
            method.invoke(abilityInfo.getAbilitySource(), player, baseLocation, params);
            return true;
        } catch (NoSuchMethodException e) {
            player.sendMessage("§cAbility method not found: " + abilityInfo.getMethodName());
        } catch (Exception e) {
            player.sendMessage("§cError executing ability: " + abilityName);
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasAbility(String abilityName) {
        return abilityRegistry.containsKey(abilityName);
    }
}

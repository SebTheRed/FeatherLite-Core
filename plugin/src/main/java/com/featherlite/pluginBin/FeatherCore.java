
package com.featherlite.pluginBin;

import com.featherlite.pluginBin.commands.*;
import com.featherlite.pluginBin.displays.DisplayPieceManager;
import com.featherlite.pluginBin.displays.IndicatorListener;
import com.featherlite.pluginBin.economy.EconomyManager;
import com.featherlite.pluginBin.essentials.PlayerDataManager;
import com.featherlite.pluginBin.essentials.commands.AdminCommands;
import com.featherlite.pluginBin.essentials.commands.HomeCommands;
import com.featherlite.pluginBin.essentials.commands.MessagingCommands;
import com.featherlite.pluginBin.essentials.commands.TeleportationCommands;
import com.featherlite.pluginBin.essentials.commands.UtilCommands;
import com.featherlite.pluginBin.essentials.teleportation.HomeManager;
import com.featherlite.pluginBin.essentials.teleportation.PlayerRespawnListener;
import com.featherlite.pluginBin.essentials.teleportation.TeleportationManager;
import com.featherlite.pluginBin.essentials.messaging.MessagingManager;
import com.featherlite.pluginBin.essentials.admin.AdminManager;
import com.featherlite.pluginBin.essentials.util.UtilManager;
import com.featherlite.pluginBin.essentials.util.PlayerJoinListenerForUtils;
import com.featherlite.pluginBin.projectiles.ProjectileManager;
import com.featherlite.pluginBin.stats.PlayerStatsManager;
import com.featherlite.pluginBin.stats.StatListeners;
import com.featherlite.pluginBin.utils.InventoryManager;
import com.featherlite.pluginBin.items.AbilityRegistry;
import com.featherlite.pluginBin.items.CooldownManager;
import com.featherlite.pluginBin.items.ItemListeners;
import com.featherlite.pluginBin.items.ItemManager;
import com.featherlite.pluginBin.items.UIManager;
import com.featherlite.pluginBin.lobbies.InstanceManager;
import com.featherlite.pluginBin.lobbies.LobbyMenuListeners;
import com.featherlite.pluginBin.lobbies.PartyManager;
import com.featherlite.pluginBin.lobbies.TeamSelectorBook;
import com.featherlite.pluginBin.menus.MenuManager;
import com.featherlite.pluginBin.particles.ParticleManager;
import com.featherlite.pluginBin.permissions.PermissionManager;
import com.featherlite.pluginBin.permissions.PlayerJoinListener;
import com.featherlite.pluginBin.placeholders.PlaceholderEconomy;
import com.featherlite.pluginBin.scoreboards.ScoreboardManager;
import com.featherlite.pluginBin.webapp.WebAppManager;
import com.featherlite.pluginBin.worlds.WorldBorderListener;
import com.featherlite.pluginBin.worlds.WorldManager;
import com.featherlite.pluginBin.zones.ZoneManager;
import com.featherlite.pluginBin.chat.ChatControlListener;
import com.featherlite.pluginBin.chat.ChatManager;
import com.featherlite.pluginBin.menus.MenuListeners;
import com.featherlite.pluginBin.lobbies.GamesManager;
import com.featherlite.pluginBin.lobbies.GamesUI;
import org.bukkit.entity.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FeatherCore extends JavaPlugin {
    private FileManager fileManager;
    private WebAppManager webAppManager;
    private PartyManager partyManager;
    private WorldManager worldManager;
    private InstanceManager instanceManager;
    private PermissionManager permissionManager;
    private UIManager uiManager;
    private ItemManager itemManager;
    private Map<UUID, String> activeSessions = new HashMap<>();
    private Map<String, Location> lobbyLocations = new HashMap<>();
    private AbilityRegistry abilityRegistry; // Add the AbilityRegistry here
    private CooldownManager cooldownManager;
    private ZoneManager zoneManager;
    private ScoreboardManager scoreboardManager;
    private EconomyManager economyManager;
    private MessagingManager messagingManager;
    private AdminManager adminManager;
    private UtilManager utilManager;
    private ParticleManager particleManager;
    private ChatManager chatManager;
    private GamesManager gamesManager;
    private GamesUI gamesUI;

    private InventoryManager inventoryManager;
    private InventoryCommands inventoryCommands;

    private TeamSelectorBook teamSelectorBook;

    private PlayerDataManager playerDataManager;
    private TeleportationManager teleportationManager;
    private HomeManager homeManager;

    private PlayerStatsManager playerStatsManager;
    
    private DisplayPieceManager displayPieceManager;

    private ProjectileManager projectileManager;

    private MenuManager menuManager;
    // private AdminManager adminManager;
    // private MessagingManager messagingManager;
    // Command handler instances
    private PartyCommands partyCommands;
    private AppCommands appCommands;
    private GameCommands gameCommands;
    private WorldCommands worldCommands;
    private PermissionsCommands permissionCommands;
    private ItemCommands itemCommands;
    private ZonesCommands zoneCommands;
    private ScoreboardCommands scoreboardCommands;
    private EconomyCommands economyCommands;
    private MessagingCommands messagingCommands;
    private AdminCommands adminCommands;
    private UtilCommands utilCommands;
    private MenuCommands menuCommands;

    private TeleportationCommands teleportationCommands;
    private HomeCommands homeCommands;

    

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadLobbyLocations();
        getLogger().info("FeatherCore Plugin Enabled!");

        playerDataManager = new PlayerDataManager(this, "player_data");
        teleportationManager = new TeleportationManager(playerDataManager, this);
        homeManager = new HomeManager(playerDataManager);

        adminManager = new AdminManager();
        messagingManager = new MessagingManager(playerDataManager);        

        utilManager = new UtilManager();

        inventoryManager = new InventoryManager(this);

        // Initialize managers

        particleManager = new ParticleManager(this);
        projectileManager = new ProjectileManager(this);
        displayPieceManager = new DisplayPieceManager(this);

        abilityRegistry = new AbilityRegistry(this, projectileManager, particleManager, displayPieceManager); // Initialize AbilityRegistry
        cooldownManager = new CooldownManager();
        fileManager = new FileManager(this);
        fileManager.loadActiveItemCategories();
        worldManager = new WorldManager(this);
        worldManager.loadPersistedWorlds();

        webAppManager = new WebAppManager(this, fileManager);
        partyManager = new PartyManager();
        itemManager = new ItemManager(this, abilityRegistry);  // Initialize ItemManager
        uiManager = new UIManager(this, itemManager); // Initialize UIManager with ItemManager
        teamSelectorBook = new TeamSelectorBook(this);
        instanceManager = new InstanceManager(partyManager, worldManager, this, teamSelectorBook);
        gamesManager = new GamesManager();
        gamesUI = new GamesUI(gamesManager, instanceManager);
        getServer().getPluginManager().registerEvents(gamesUI, this);

        permissionManager = new PermissionManager(this, playerDataManager); // Initialize the permission manager
        zoneManager = new ZoneManager(this);
        scoreboardManager = new ScoreboardManager(this);

        economyManager = new EconomyManager(this, playerDataManager);
        menuManager = new MenuManager(this, economyManager);
        getServer().getPluginManager().registerEvents(new MenuListeners(this, menuManager), this);


        chatManager = new ChatManager(this);
        getServer().getPluginManager().registerEvents(new ChatControlListener(this, chatManager, permissionManager), this);

        partyManager.setInstanceManager(instanceManager);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(permissionManager), this);
        getServer().getPluginManager().registerEvents(new ItemListeners(abilityRegistry, cooldownManager, this), this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListenerForUtils(playerDataManager, displayPieceManager, inventoryManager), this);

        getServer().getPluginManager().registerEvents(new WorldBorderListener(worldManager), this);

        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(teleportationManager, this), this);

        playerStatsManager = new PlayerStatsManager(this);
        getServer().getPluginManager().registerEvents(new StatListeners(playerStatsManager), this);

        getServer().getPluginManager().registerEvents(new LobbyMenuListeners(this, instanceManager, teamSelectorBook), this);

        new IndicatorListener(this, displayPieceManager);


        // Initialize command handlers
        partyCommands = new PartyCommands(partyManager);
        appCommands = new AppCommands(webAppManager, activeSessions);
        gameCommands = new GameCommands(instanceManager, gamesManager, gamesUI, teamSelectorBook);
        worldCommands = new WorldCommands(worldManager);
        permissionCommands = new PermissionsCommands(permissionManager);
        itemCommands = new ItemCommands(uiManager, itemManager, this); // Only pass the main plugin instance
        zoneCommands = new ZonesCommands(zoneManager ,this);
        scoreboardCommands = new ScoreboardCommands(scoreboardManager);
        economyCommands = new EconomyCommands(economyManager, this);
        menuCommands = new MenuCommands(menuManager, this);
        teleportationCommands = new TeleportationCommands(teleportationManager, this);
        homeCommands = new HomeCommands(homeManager, teleportationManager);

        adminCommands = new AdminCommands(adminManager);
        messagingCommands = new MessagingCommands(messagingManager);
        utilCommands = new UtilCommands(utilManager, playerDataManager);

        inventoryCommands = new InventoryCommands(inventoryManager);


        //Set Managers for Placeholders
        PlaceholderEconomy.setEconomyManager(economyManager);



        // Register commands
        getCommand("app").setExecutor(this);
        getCommand("party").setExecutor(this);
        getCommand("party").setTabCompleter(partyCommands);
        getCommand("game").setExecutor(this);
        getCommand("game").setTabCompleter(gameCommands);
        getCommand("games").setExecutor(this);
        getCommand("games").setTabCompleter(gameCommands);
        getCommand("world").setExecutor(this);
        getCommand("world").setTabCompleter(worldCommands);
        getCommand("perms").setExecutor(this);
        getCommand("perms").setTabCompleter(permissionCommands);
        getCommand("items").setExecutor(this);
        getCommand("zone").setExecutor(this);
        getCommand("zone").setTabCompleter(zoneCommands);
        getCommand("board").setExecutor(this);
        getCommand("board").setTabCompleter(scoreboardCommands);
        getCommand("inventory").setExecutor(this);
        getCommand("inv").setExecutor(this);
        getCommand("menu").setExecutor(this);
        getCommand("menu").setTabCompleter(menuCommands);
        getCommand("menus").setExecutor(this);
        getCommand("menus").setTabCompleter(menuCommands);
        getCommand("eco").setExecutor(this);
        getCommand("eco").setTabCompleter(economyCommands);
        getCommand("bal").setExecutor(this);
        getCommand("baltop").setExecutor(this);
        // Essentials command and completer registries.
        getCommand("tppos").setExecutor(this);
        getCommand("tppos").setTabCompleter(teleportationCommands);
        getCommand("tp").setExecutor(this);
        getCommand("tp").setTabCompleter(teleportationCommands);
        getCommand("tphere").setExecutor(this);
        getCommand("tphere").setTabCompleter(teleportationCommands);
        getCommand("tpall").setExecutor(this);
        getCommand("tpall").setTabCompleter(teleportationCommands);
        getCommand("tpa").setExecutor(this);
        getCommand("tpa").setTabCompleter(teleportationCommands);
        getCommand("tpahere").setExecutor(this);
        getCommand("tpahere").setTabCompleter(teleportationCommands);
        getCommand("tpaccept").setExecutor(this);
        getCommand("tpaccept").setTabCompleter(teleportationCommands);
        getCommand("tpadeny").setExecutor(this);
        getCommand("tpadeny").setTabCompleter(teleportationCommands);
        getCommand("tpacancel").setExecutor(this);
        getCommand("tpacancel").setTabCompleter(teleportationCommands);
        getCommand("tpr").setExecutor(this);
        getCommand("tpr").setTabCompleter(teleportationCommands);
        getCommand("rtp").setExecutor(this);
        getCommand("rtp").setTabCompleter(teleportationCommands);
        getCommand("spawn").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
        getCommand("back").setExecutor(this);

        getCommand("sethome").setExecutor(this);
        getCommand("sethome").setTabCompleter(homeCommands);
        getCommand("home").setExecutor(this);
        getCommand("home").setTabCompleter(homeCommands);
        getCommand("delhome").setExecutor(this);
        getCommand("delhome").setTabCompleter(homeCommands);
        getCommand("homes").setExecutor(this);
        getCommand("homes").setTabCompleter(homeCommands);

        getCommand("msg").setExecutor(this);
        getCommand("msg").setTabCompleter(messagingCommands);
        getCommand("r").setExecutor(this);
        getCommand("ignore").setExecutor(this);
        getCommand("ignore").setTabCompleter(messagingCommands);
        getCommand("msgtoggle").setExecutor(this);
        getCommand("broadcast").setExecutor(this);

        getCommand("repair").setExecutor(this);
        getCommand("enchant").setExecutor(this);
        getCommand("enchant").setTabCompleter(adminCommands);
        getCommand("exp").setExecutor(this);
        getCommand("exp").setTabCompleter(adminCommands);
        getCommand("give").setExecutor(this);
        getCommand("give").setTabCompleter(adminCommands);
        getCommand("kill").setExecutor(this);
        getCommand("kill").setTabCompleter(adminCommands);
        getCommand("killall").setExecutor(this);
        getCommand("remove").setExecutor(this);
        getCommand("killall").setTabCompleter(adminCommands);
        getCommand("remove").setTabCompleter(adminCommands);
        getCommand("sudo").setExecutor(this);
        getCommand("weather").setExecutor(this);
        getCommand("weather").setTabCompleter(adminCommands);
        getCommand("time").setExecutor(this);
        getCommand("time").setTabCompleter(adminCommands);
        getCommand("god").setExecutor(this);

        getCommand("fly").setExecutor(this);
        getCommand("speed").setExecutor(this);
        getCommand("flyspeed").setExecutor(this);
        getCommand("gamemode").setExecutor(this);
        getCommand("gamemode").setTabCompleter(utilCommands);
        getCommand("gm").setExecutor(this);
        getCommand("gm").setTabCompleter(utilCommands);
        getCommand("heal").setExecutor(this);
        getCommand("feed").setExecutor(this);
        getCommand("rest").setExecutor(this);
        getCommand("afk").setExecutor(this);
        getCommand("enderchest").setExecutor(this);
        getCommand("ec").setExecutor(this);
        getCommand("trash").setExecutor(this);
        getCommand("top").setExecutor(this);
        getCommand("hat").setExecutor(this);
        getCommand("nick").setExecutor(this);
        getCommand("nick").setTabCompleter(utilCommands);
        getCommand("nickname").setExecutor(this);
        getCommand("nickname").setTabCompleter(utilCommands);
        getCommand("realname").setExecutor(this);
        getCommand("list").setExecutor(this);
        getCommand("near").setExecutor(this);
        getCommand("getpos").setExecutor(this);
        getCommand("ping").setExecutor(this);
        getCommand("seen").setExecutor(this);
        getCommand("workbench").setExecutor(this);
        getCommand("wb").setExecutor(this);
        getCommand("craft").setExecutor(this);
        getCommand("anvil").setExecutor(this);
        getCommand("cartographytable").setExecutor(this);
        getCommand("grindstone").setExecutor(this);
        getCommand("loom").setExecutor(this);
        getCommand("smithingtable").setExecutor(this);
        getCommand("smithing").setExecutor(this);
        getCommand("stonecutter").setExecutor(this);
        getCommand("ptime").setExecutor(this);
        getCommand("pweather").setExecutor(this);



    }

    @Override
    public void onDisable() {
        getLogger().info("FeatherCore Plugin Disabled!");
        Bukkit.getOnlinePlayers().forEach(playerStatsManager::savePlayerStats);

        displayPieceManager.clearAllDisplays();

        // Additional cleanup for lingering entities
        Bukkit.getWorlds().forEach(world -> {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Display) {
                    entity.remove();
                    this.getLogger().info("Force-removed lingering display entity: " + entity.getUniqueId());
                }
            }
        });

    }



    private void loadLobbyLocations() {
        if (getConfig().isConfigurationSection("lobbyLocations")) {
            ConfigurationSection section = getConfig().getConfigurationSection("lobbyLocations");

            for (String name : section.getKeys(false)) {
                String worldName = section.getString(name + ".world");
                double x = section.getDouble(name + ".x");
                double y = section.getDouble(name + ".y");
                double z = section.getDouble(name + ".z");

                if (Bukkit.getWorld(worldName) == null) {
                    getLogger().warning("World " + worldName + " for lobby " + name + " does not exist!");
                    continue;
                }

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                lobbyLocations.put(name, location);
            }
        } else {
            getLogger().warning("No 'lobbyLocations' section found in config.yml!");
        }
    }

    public Location getLobbyLocation(String name) {
        return lobbyLocations.get(name);
    }

    /*
     * 
     * Below we are exposing managers for API calls. 
     */
    // ITEM UI MANAGER - SHOULD CHANGE THIS NAME
    public UIManager getUiManager() {
        return uiManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    public ParticleManager getParticleManager() {
        return particleManager;
    }
    
    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public InstanceManager getInstanceManager() {
        return instanceManager;
    }

    public GamesManager getGamesManager() {
        return gamesManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public TeleportationManager getTeleportationManager() {
        return teleportationManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public PlayerStatsManager getPlayerStatsManager() {
        return playerStatsManager;
    }

    public DisplayPieceManager getDisplayPieceManager() {
        return displayPieceManager;
    }





    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            // Player player = (Player) sender;
            boolean isPlayer = sender instanceof Player;
            
            switch (command.getName().toLowerCase()) {
                case "party":
                    return partyCommands.handlePartyCommands(sender, args, isPlayer, this);
                case "app":
                    return appCommands.handleAppCommands(sender, args, isPlayer, this);
                case "game":
                case "games":
                    return gameCommands.handleGameCommands(sender, args, isPlayer, this);
                case "world":
                    return worldCommands.handleWorldCommands(sender, args, isPlayer, this);
                case "perms":
                    return permissionCommands.handlePermissionsCommands(sender, args, isPlayer);
                case "items":
                    return itemCommands.handleItemCommands(sender, args, isPlayer, this);
                case "zone":
                    return zoneCommands.handleZoneCommands(sender, args, isPlayer);
                case "board":
                    return scoreboardCommands.handleScoreboardCommands(sender, args, isPlayer);
                case "inv":
                case "inventory":
                    return inventoryCommands.handleInventoryCommands(sender, args, isPlayer);
                case "menu":
                case "menus":
                    return menuCommands.handleMenuCommands(sender, command, label, args, isPlayer);
                case "eco":
                case "bal":
                case "baltop":
                    return economyCommands.handleEconomyCommands(label, sender, args, isPlayer);

                // Essetials cases
                case "tppos":
                case "tp":
                case "tphere":
                case "tpall":
                case "tpa":
                case "tpahere":
                case "tpaccept":
                case "tpadeny":
                case "tpacancel":
                case "back":
                case "tpr":
                case "spawn":
                case "setspawn":
                    return teleportationCommands.handleTeleportCommands(sender, command, label, args, isPlayer);
                case "home":
                case "sethome":
                case "delhome":
                case "homes":
                    return homeCommands.handleHomeCommands(sender, command, label, args, isPlayer);
                case "msg":
                case "message":
                case "dm":
                case "r":
                case "reply":
                case "broadcast":
                case "announce":
                case "msgtoggle":
                case "dmtoggle":
                case "ignore":
                    return messagingCommands.handleMessagingCommands(sender, command, label, args, isPlayer);
                case "enchant":
                case "exp":
                case "give":
                case "kill":
                case "killall":
                case "remove":
                case "sudo":
                case "weather":
                case "time":
                case "god":
                    return adminCommands.handleAdminCommands(sender, command, label, args, isPlayer);
                case "fly":
                case "speed":
                case "flyspeed":
                case "gamemode":
                case "gm":
                case "heal":
                case "feed":
                case "rest":
                case "repair":
                case "afk":
                case "enderchest":
                case "ec":
                case "trash":
                case "top":
                case "hat":
                case "nick":
                case "nickname":
                case "realname":
                case "list":
                case "near":
                case "getpos":
                case "ping":
                case "seen":
                case "workbench":
                case "wb":
                case "craft":
                case "anvil":
                case "cartographytable":
                case "grindstone":
                case "loom":
                case "smithing":
                case "smithingtable":
                case "stonecutter":
                case "ptime":
                case "pweather":
                    return utilCommands.handleUtilCommands(sender, command, label, args, isPlayer);
                    
            }
        }
        return false;
    }

    public void invalidateSession(UUID playerUUID) {
        activeSessions.remove(playerUUID);
    }
}

package com.featherlite.pluginBin.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;

import com.featherlite.pluginBin.economy.EconomyManager;
import com.featherlite.pluginBin.utils.ColorUtils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MenuManager {
    private final JavaPlugin plugin;
    private final EconomyManager economyManager;
    private final Map<String, Menu> menus = new HashMap<>();
    private final Map<String, String> inventoryTitleToMenuId = new HashMap<>();


    public MenuManager(JavaPlugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
        ensureDefaultMenuFile();
        loadMenusFromAllPlugins();
    }

    // Method placeholders
    public Map<String, Menu> getMenus() { return menus; }

    public Map<String, String> getInventoryTitleToMenuId() {
        return inventoryTitleToMenuId;
    }


    public Inventory buildInventory(MenuPage page, int slots, String id, String title) {
        ColorUtils colorUtils = new ColorUtils();
        String colorParsedTitle = colorUtils.parseColors(title);
        Inventory inventory = Bukkit.createInventory(null, slots, colorParsedTitle);
        String strippedInventoryName = ChatColor.stripColor(colorParsedTitle);
        // Populate inventory with buttons
        page.getItems().forEach((slot, button) -> {
            if (button instanceof CommandButton) {
                inventory.setItem(slot, ((CommandButton) button).getIcon());
            } else if (button instanceof RouteButton) {
                inventory.setItem(slot, ((RouteButton) button).getIcon());
            } else if (button instanceof BuyButton) {
                inventory.setItem(slot, ((BuyButton) button).getIcon());
            }
        });
        inventoryTitleToMenuId.put(strippedInventoryName, id); // Store the mapping (new Map added below)


        return inventory;
    }


    public void ensureDefaultMenuFile() {
        File menuFolder = new File(plugin.getDataFolder(), "menus");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }
    
        File defaultMenuFile = new File(menuFolder, "featherlite-help.yml");
        if (!defaultMenuFile.exists()) {
            plugin.getLogger().info("Default menu file not found. Copying featherlite-help.yml from resources...");
            try {
                plugin.saveResource("menus/featherlite-help.yml", false);
                plugin.getLogger().info("Successfully copied featherlite-help.yml.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to copy featherlite-help.yml: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public void loadMenusFromAllPlugins() {
        menus.clear();
    
        // Load menus from the core plugin first
        loadMenusFromPlugin(plugin);
    
        // Load menus from all other plugins
        for (Plugin installedPlugin : Bukkit.getPluginManager().getPlugins()) {
            if (installedPlugin.isEnabled() && !installedPlugin.equals(plugin)) {
                loadMenusFromPlugin(installedPlugin);
            }
        }
    }
    
    public void loadMenusFromPlugin(Plugin plugin) {
        File pluginMenuFolder = new File(plugin.getDataFolder(), "menus");
        if (pluginMenuFolder.exists() && pluginMenuFolder.isDirectory()) {
            plugin.getLogger().info("Loading menus from " + plugin.getName());
            for (File file : pluginMenuFolder.listFiles()) {
                if (file.getName().endsWith(".yml")) {
                    try {
                        Menu menu = parseMenu(file);
                        if (menu != null) {
                            menus.put(menu.getID(), menu);
                            plugin.getLogger().info("Loaded menu: " + menu.getID());
                        }
                    } catch (Exception e) {
                        plugin.getLogger().severe("Failed to load menu from " + file.getName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            plugin.getLogger().info("No menus folder found for " + plugin.getName());
        }
    }
    


    private Menu parseMenu(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String id = config.getString("menu.id", "Unknown ID");
        int slots = config.getInt("menu.slots", 27);
        plugin.getLogger().info("Parsing menu: " + id);
    
        Menu menu = new Menu(id, slots);
    
        ConfigurationSection pagesSection = config.getConfigurationSection("menu.pages");
        if (pagesSection != null) {
            for (String pageName : pagesSection.getKeys(false)) {
                ConfigurationSection pageSection = pagesSection.getConfigurationSection(pageName);
                String pageTitle = pageSection.getString("title", "Unknown Page");
                plugin.getLogger().info("Parsing page: " + pageName + " | Title: " + pageTitle);
    
                MenuPage page = new MenuPage(pageTitle);
    
                ConfigurationSection itemsSection = pageSection.getConfigurationSection("items");
                if (itemsSection != null) {
                    for (String key : itemsSection.getKeys(false)) {
                        int slot = Integer.parseInt(key);
                        MenuButton button = parseButton(itemsSection.getConfigurationSection(key));
                        if (button != null) {
                            plugin.getLogger().info("Adding button to page: " + pageName + " | Slot: " + slot + " | Type: " + button.getClass().getSimpleName());
                            page.addItem(slot, button);
                        }
                    }
                }
    
                menu.addPage(pageName, page);
            }
        }
    
        return menu;
    }
    

    private MenuButton parseButton(ConfigurationSection section) {
        String type = section.getString("type");
        String materialName = section.getString("icon.material", "STONE");
        int amount = section.getInt("icon.amount", 1);

        // Build ItemStack
        ItemStack icon = new ItemStack(Material.valueOf(materialName.toUpperCase()), amount);
        if (icon.getItemMeta() != null) {
            ItemMeta meta = icon.getItemMeta();

            // Set display name
            if (section.contains("displayName")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("displayName")));
            }

            // Set lore
            if (section.contains("lore")) {
                List<String> lore = section.getStringList("lore");
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }

            icon.setItemMeta(meta);
        }

        // Parse button types and return the correct button
        switch (type) {
            case "command-button":
                return new CommandButton(icon, section.getString("command"));
    
            case "route-button":
                return new RouteButton(icon, section.getString("route"), this);
    
            case "buy-button":
                String currency = section.getString("currency");
                double cost = section.getDouble("cost");
                String command = section.getString("command");
                if (command == null || command.isEmpty()) {
                    plugin.getLogger().warning("BuyButton missing command!");
                    return null;
                }
                return new BuyButton(icon, currency, cost, command, economyManager, plugin);
    
            default:
                plugin.getLogger().warning("Unknown button type: " + type);
                return null;
        }
    }

    

    


}


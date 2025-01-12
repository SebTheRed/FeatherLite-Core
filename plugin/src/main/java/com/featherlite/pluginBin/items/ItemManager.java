
package com.featherlite.pluginBin.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemManager {
    private final JavaPlugin plugin;
    private final AbilityRegistry abilityRegistry;
    private final Map<String, Map<String, ItemStack>> categorizedItems = new HashMap<>(); // category -> itemName -> ItemStack
    private final Map<String, ItemStack> categoryIcons = new HashMap<>(); // category -> icon ItemStack
    private final Map<String, String> loreTemplates = new HashMap<>();


    public ItemManager(JavaPlugin plugin, AbilityRegistry abilityRegistry) {
        this.plugin = plugin;
        this.abilityRegistry = abilityRegistry; // Pass plugin here
        
        loadLoreTemplates();

        loadAllPluginItems();


    }


    // Retrieve an ItemStack by category and item name
    public ItemStack getItem(String category, String itemName) {
        Map<String, ItemStack> itemsInCategory = categorizedItems.get(category);
        if (itemsInCategory != null) {
            return itemsInCategory.get(itemName);
        }
        return null; // Return null if the category or item is not found
    }

    // Load lore templates from config.yml into the map
    private void loadLoreTemplates() {
        // Define the file path
        File loreFile = new File(plugin.getDataFolder(), "item-lore-templates.yml");
    
        // If the file doesn't exist, copy it from resources
        if (!loreFile.exists()) {
            plugin.getLogger().info("item-lore-templates.yml not found. Creating a default version...");
            plugin.saveResource("item-lore-templates.yml", false); // Copy the file from /resources
        }
    
        // Load the file as a configuration
        FileConfiguration loreConfig = YamlConfiguration.loadConfiguration(loreFile);
        ConfigurationSection loreSection = loreConfig.getConfigurationSection("lore_customization");
    
        if (loreSection != null) {
            // Populate the loreTemplates map
            for (String key : loreSection.getKeys(false)) {
                String value = loreSection.getString(key);
                loreTemplates.put(key, value);
                // plugin.getLogger().info("Loaded key: " + key + " | Value: " + value); // Debug log
            
            }
        } else {
            plugin.getLogger().warning("No 'lore_customization' section found in item-lore-templates.yml");
        }
    }

    // Method to retrieve a template for a given key (with a fallback if missing)
    public String getLoreTemplate(String key) {
        return loreTemplates.getOrDefault(key, "§7{key}: {value}"); // Default format if not found
    }

    public void reloadItems(UIManager uiManager) {
        categorizedItems.clear();
        categoryIcons.clear();
        loadLoreTemplates();
        loadAllPluginItems();
        plugin.getLogger().info("All items have been reloaded successfully.");
        
        // Clear UI-related caches
        uiManager.clearCategoryInventories();
    }

    /**
     * Loads items from the /items/ directories of all plugins based on their active-item-categories.
     */
    private void loadAllPluginItems() {
        // Iterate through all plugins and load items from their /items/ folders
        for (Plugin otherPlugin : Bukkit.getPluginManager().getPlugins()) {
            loadItemsFromPlugin(otherPlugin);
        }
    }


    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    /**
     * Loads items from the specified plugin's items folder if they are listed in active-item-categories in its config.yml.
     */
    private void loadItemsFromPlugin(Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.plugin.getLogger().info("No config.yml found for " + plugin.getName());
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        List<String> activeCategories = config.getStringList("active-item-categories");

        if (activeCategories.isEmpty()) {
            this.plugin.getLogger().warning("No active item categories found for " + plugin.getName());
            return;
        }

        // Load items from each active category
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        for (String categoryFile : activeCategories) {
            File itemFile = new File(itemsFolder, categoryFile);
            if (itemFile.exists()) {
                loadItemsFromFile(itemFile, plugin.getName());
            } else {
                this.plugin.getLogger().warning("Item file " + categoryFile + " not found in " + itemsFolder.getPath() + " for " + plugin.getName());
            }
        }
    }

    /**
     * Loads items from a specific category file and populates the category icons and categorized items.
     */
    private void loadItemsFromFile(File file, String pluginName) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String categoryName = config.getString("categoryInfo.category-name", file.getName().replace(".yml", ""));

        // Load category icon if specified
        String iconMaterial = config.getString("categoryInfo.icon", "STONE");
        Material material = Material.matchMaterial(iconMaterial);
        if (material != null) {
            ItemStack categoryIcon = new ItemStack(material);
            categoryIcons.put(categoryName, categoryIcon);
        }

        // Load each item within the category using ItemCreator
        Map<String, ItemStack> items = new HashMap<>();
        if (config.isConfigurationSection("items")) {
            ConfigurationSection itemsSection = config.getConfigurationSection("items");
            for (String itemName : itemsSection.getKeys(false)) {  // Use false to get top-level keys
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemName);
                if (itemSection != null) {
                    ItemStack itemStack = createItemFromConfig(itemSection);
                    items.put(itemName, itemStack);
                }
            }
        }
        categorizedItems.put(categoryName, items);
    }

    /**
     * Creates an ItemStack from the configuration file's item data using ItemCreator.
     */
public ItemStack createItemFromConfig(ConfigurationSection itemSection) {
    // Get the material, defaulting to STONE if not specified
    String itemType = itemSection.getString("type", "").toLowerCase();

    String materialName = itemSection.getString("material", "STONE");
    Material material = Material.matchMaterial(materialName);
    if (material == null) material = Material.STONE;

    // Create the item using ItemCreator
    ItemCreator itemCreator = new ItemCreator(plugin, material, loreTemplates);

    // Set display name
    String displayName = itemSection.getString("name");
    if (displayName != null) {
        itemCreator.setName(displayName.replace("&", "§")); // Convert color codes
    }

    // Set custom model data if provided
    if (itemSection.contains("customModelData")) {
        itemCreator.setCustomModelData(itemSection.getInt("customModelData"));
    }

    // Mark as unbreakable if specified
    if (itemSection.getBoolean("unbreakable", false)) {
        itemCreator.setUnbreakable(true);
        itemCreator.addLore(itemCreator.getFormattedLore("unbreakable", Map.of("value", String.valueOf(true))));
    }

    // // **Add item ability and parameters**
    // if (itemSection.contains("itemAbility")) {
    //     String abilityName = itemSection.getString("itemAbility");
    //     itemCreator.setItemAbility(abilityName);
    
    //     // Store additional parameters prefixed with itemAbility_
    //     for (String key : itemSection.getKeys(false)) {
    //         if (key.startsWith("itemAbility_")) {
    //             String paramName = key.substring("itemAbility_".length());
    //             String paramValue = itemSection.getString(key);
    //             plugin.getLogger().info("Loading parameter " + paramName + " with value: " + paramValue); // Log the parameter from config
    //             itemCreator.setAbilityParam(paramName, paramValue);
    //         }
    //     }
    // }


    // Add enchantments if specified
    if (itemSection.isConfigurationSection("enchantments")) {
        ConfigurationSection enchantsSection = itemSection.getConfigurationSection("enchantments");
        for (String enchantName : enchantsSection.getKeys(false)) {
            NamespacedKey key = NamespacedKey.minecraft(enchantName.toLowerCase());
            Enchantment enchantment = Registry.ENCHANTMENT.get(key); // Corrected approach
            int level = enchantsSection.getInt(enchantName);
            if (enchantment != null) {
                itemCreator.addEnchantment(enchantment, level);
            } else {
                plugin.getLogger().warning("Invalid enchantment: " + enchantName);
            }
        }
    }

    if (itemSection.contains("effect")) {
        String effectName = itemSection.getString("effect").toUpperCase();
        // plugin.getLogger().info("Attempting to set effect: " + effectName);
    
        PotionEffectType effectType = Registry.EFFECT.get(NamespacedKey.minecraft(effectName.toLowerCase()));
        
        if (effectType != null) {
            int duration = itemSection.getInt("duration", 1) * 20; // Convert seconds to ticks
            int amplifier = itemSection.getInt("amplifier", 0);
    
            // Log the extracted effect data for debugging
            // plugin.getLogger().info("Effect found: " + effectName +
            //                         " | Type: " + effectType.getName() +
            //                         " | Duration: " + duration +
            //                         " | Amplifier: " + amplifier);
    
            if (itemType.equals("potion")) {
                // Apply effect as potion metadata
                // plugin.getLogger().info("Applying effect to potion item.");
                itemCreator.setPotionEffect(effectType, duration, amplifier);
            } else if (itemType.equals("food") || itemType.equals("consumable")) {
                // Store effect in PersistentDataContainer for non-potion items
                // plugin.getLogger().info("Applying effect to food/consumable item.");
                itemCreator.setFoodEffect(effectType, duration, amplifier);
            } else {
                plugin.getLogger().warning("Effect can only be used on type 'potion', 'consumable', or 'food': " + effectName);
            }
        } else {
            plugin.getLogger().warning("Invalid effect name: " + effectName);
        }
    }

    
    if (itemSection.contains("dye")) {
        String[] dyeComponents = itemSection.getString("dye").split(",");
        if (dyeComponents.length == 3) {
            try {
                int red = Integer.parseInt(dyeComponents[0].trim());
                int green = Integer.parseInt(dyeComponents[1].trim());
                int blue = Integer.parseInt(dyeComponents[2].trim());
                itemCreator.setLeatherArmorColor(red, green, blue);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid dye color values. Expected format: 'dye: 0, 255, 20'.");
            }
        } else {
            plugin.getLogger().warning("Invalid dye format. Expected 3 comma-separated RGB values.");
        }
    }


    // Add attributes for combat items
    if (itemSection.contains("attackDamage")) {
        double dubVal = itemSection.getDouble("attackDamage");
        itemCreator.setAttribute(Attribute.GENERIC_ATTACK_DAMAGE, dubVal, Operation.ADD_NUMBER, "ANY");
    }
    
    if (itemSection.contains("attackKnockback")) {
        double dubVal = itemSection.getDouble("attackKnockback");
        itemCreator.setAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK, dubVal,Operation.ADD_NUMBER, "ANY");
    }

    if (itemSection.contains("attackSpeed")) {
        double dubVal = itemSection.getDouble("attackSpeed");
        itemCreator.setAttribute(Attribute.GENERIC_ATTACK_SPEED, dubVal, Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("burningTime")) {
        double dubVal = itemSection.getDouble("burningTime");
        itemCreator.setAttribute(Attribute.GENERIC_BURNING_TIME, dubVal,Operation.ADD_NUMBER, "ANY");
    }


    if (itemSection.contains("armor")) {
        double dubVal = itemSection.getDouble("armor");
        itemCreator.setAttribute(Attribute.GENERIC_ARMOR, dubVal, Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("armorToughness")) {
        double dubVal = itemSection.getDouble("armorToughness");
        itemCreator.setAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS, dubVal, Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("explosionKnockbackResistance")) {
        double dubVal = itemSection.getDouble("explosionKnockbackResistance");
        itemCreator.setAttribute(Attribute.GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("knockbackResistance")) {
        double dubVal = itemSection.getDouble("knockbackResistance");
        itemCreator.setAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    


    if (itemSection.contains("scale")) {
        double dubVal = itemSection.getDouble("scale");
        itemCreator.setAttribute(Attribute.GENERIC_SCALE, dubVal, Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("fallDamageMultiplier")) {
        double dubVal = itemSection.getDouble("fallDamageMultiplier");
        itemCreator.setAttribute(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("flyingSpeed")) {
        double dubVal = itemSection.getDouble("flyingSpeed");
        itemCreator.setAttribute(Attribute.GENERIC_FLYING_SPEED, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("gravity")) {
        double dubVal = itemSection.getDouble("gravity");
        itemCreator.setAttribute(Attribute.GENERIC_GRAVITY, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("jumpStrength")) {
        double dubVal = itemSection.getDouble("jumpStrength");
        itemCreator.setAttribute(Attribute.GENERIC_JUMP_STRENGTH, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("luck")) {
        double dubVal = itemSection.getDouble("luck");
        itemCreator.setAttribute(Attribute.GENERIC_LUCK, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("maxAbsorption")) {
        double dubVal = itemSection.getDouble("maxAbsorption");
        itemCreator.setAttribute(Attribute.GENERIC_MAX_ABSORPTION, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("maxHealth")) {
        double dubVal = itemSection.getDouble("maxHealth");
        itemCreator.setAttribute(Attribute.GENERIC_MAX_HEALTH, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("movementEfficiency")) {
        double dubVal = itemSection.getDouble("movementEfficiency");
        itemCreator.setAttribute(Attribute.GENERIC_MOVEMENT_EFFICIENCY, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("movementSpeed")) {
        double dubVal = itemSection.getDouble("movementSpeed");
        itemCreator.setAttribute(Attribute.GENERIC_MOVEMENT_SPEED, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("oxygenBonus")) {
        double dubVal = itemSection.getDouble("oxygenBonus");
        itemCreator.setAttribute(Attribute.GENERIC_OXYGEN_BONUS, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("safeFallDistance")) {
        double dubVal = itemSection.getDouble("safeFallDistance");
        itemCreator.setAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("stepHeight")) {
        double dubVal = itemSection.getDouble("stepHeight");
        itemCreator.setAttribute(Attribute.GENERIC_STEP_HEIGHT, dubVal,Operation.ADD_NUMBER, "ANY");
    }
    if (itemSection.contains("waterMovementEfficiecy")) {
        double dubVal = itemSection.getDouble("waterMovementEfficiecy");
        itemCreator.setAttribute(Attribute.GENERIC_WATER_MOVEMENT_EFFICIENCY, dubVal,Operation.ADD_NUMBER, "ANY");
    }



    if (itemSection.contains("arrowVelocity") && (material == Material.BOW || material == Material.CROSSBOW)) {
        double arrowVelocity = itemSection.getDouble("arrowVelocity");
        itemCreator.setArrowVelocity(arrowVelocity);
    }

       // Parsing abilities
   if (itemSection.isConfigurationSection("abilities")) {
    ConfigurationSection abilitiesSection = itemSection.getConfigurationSection("abilities");

        for (String trigger : abilitiesSection.getKeys(false)) {
            ConfigurationSection triggerSection = abilitiesSection.getConfigurationSection(trigger);
            if (triggerSection != null) {
                String methodName = triggerSection.getString("method");
                String title = triggerSection.getString("title"); // For lore display
                Map<String, String> params = new HashMap<>();

                for (String paramKey : triggerSection.getKeys(false)) {
                    if (!paramKey.equals("method") && !paramKey.equals("title")) {
                        String paramValue = triggerSection.getString(paramKey);
                        params.put(paramKey, paramValue);
                    }
                }

                // Add ability to the item
                itemCreator.setAbility(trigger, methodName, title, params);
            }
        }
    }

    if ("block".equals(itemType)) {
        boolean explosionProof = itemSection.getBoolean("explosionProof", false);
        itemCreator.setExplosionProof(explosionProof);
        
        // Load place ability and parameters
        if (itemSection.contains("placeAbility")) {
            String placeAbility = itemSection.getString("placeAbility");
            Map<String, String> placeParams = new HashMap<>();
            for (String key : itemSection.getKeys(false)) {
                if (key.startsWith("placeAbility_")) {
                    placeParams.put(key.substring("placeAbility_".length()), itemSection.getString(key));
                }
            }
            itemCreator.setPlaceAbility(placeAbility, placeParams);
        }

        // Load break ability and parameters
        if (itemSection.contains("breakAbility")) {
            String breakAbility = itemSection.getString("breakAbility");
            Map<String, String> breakParams = new HashMap<>();
            for (String key : itemSection.getKeys(false)) {
                if (key.startsWith("breakAbility_")) {
                    breakParams.put(key.substring("breakAbility_".length()), itemSection.getString(key));
                }
            }
            itemCreator.setBreakAbility(breakAbility, breakParams);
        }

        boolean temporaryOnPlace = itemSection.getBoolean("temporaryOnPlace", false);
        itemCreator.setTemporaryOnPlace(temporaryOnPlace);
    }


    // Set lore if specified
    List<String> lore = itemSection.getStringList("lore");
    if (!lore.isEmpty()) {
        itemCreator.addLore(""); // Add an empty line for spacing
        for (String loreLine : lore) {
            itemCreator.addLore(itemCreator.translateColorCodes(loreLine)); // Convert color codes
        }
    }

    // Finally, create the item
    return itemCreator.create();
}


    // Public methods for UI and external access

    public Set<String> getCategories() {
        return categorizedItems.keySet();
    }

    public ItemStack getCategoryIcon(String category) {
        return categoryIcons.getOrDefault(category, new ItemStack(Material.BARRIER));
    }

    public Map<String, ItemStack> getItemsInCategory(String category) {
        return categorizedItems.getOrDefault(category, new HashMap<>());
    }


    

}
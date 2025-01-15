package com.featherlite.pluginBin.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.AttributeModifier.Operation;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Color;

import java.util.Map;
import java.util.Collection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.PotionMeta;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;


public class ItemCreator {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final PersistentDataContainer dataContainer;
    private final JavaPlugin plugin;
    private final Multimap<Attribute, AttributeModifier> attributeModifiers;
    private final Map<String, String> loreTemplates; // Add lore templates map
    private final boolean isDebuggerOn;


    public ItemCreator(JavaPlugin plugin, Material material, Map<String, String> loreTemplates, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.dataContainer = itemMeta.getPersistentDataContainer();
        this.attributeModifiers = HashMultimap.create();
        this.loreTemplates = loreTemplates; // Assign lore templates
        this.isDebuggerOn = isDebuggerOn;

    }

    // Set display name
    public ItemCreator setName(String name) {
        itemMeta.setDisplayName(name);
        return this;
    }

    // Set custom model data
    public ItemCreator setCustomModelData(int data) {
        itemMeta.setCustomModelData(data);
        return this;
    }


    // Set item category for interpretation
    public ItemCreator setItemCategory(String category) {
        dataContainer.set(new NamespacedKey(plugin, "itemCategory"), PersistentDataType.STRING, category);
        return this;
    }

        public ItemCreator setLeatherArmorColor(int red, int green, int blue) {
        // Validate that the item is leather armor
        if (!(itemMeta instanceof LeatherArmorMeta)) {
            plugin.getLogger().warning("Dye color can only be applied to leather armor: " + itemStack.getType());
            return this;
        }

        // Validate RGB values
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            plugin.getLogger().warning("RGB values must be between 0 and 255.");
            return this;
        }

        // Apply the color
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) itemMeta;
        leatherMeta.setColor(Color.fromRGB(red, green, blue));
        itemStack.setItemMeta(leatherMeta);
        return this;
    }

    // Set draw speed
    public ItemCreator setDrawSpeed(double drawSpeed) {
        dataContainer.set(new NamespacedKey(plugin, "drawSpeed"), PersistentDataType.DOUBLE, (drawSpeed));
        addLore(getFormattedLore("drawSpeed", Map.of("value", String.valueOf(drawSpeed))));
        return this;
    }

    // Set arrow velocity
    public ItemCreator setArrowVelocity(double arrowVelocity) {
        dataContainer.set(new NamespacedKey(plugin, "arrowVelocity"), PersistentDataType.DOUBLE, (arrowVelocity));
        // addFormattedLore("arrowVelocity", Map.of("value", String.valueOf(arrowVelocity)));
        addLore(getFormattedLore("arrowVelocity", Map.of("value", String.valueOf(arrowVelocity))));
        return this;
    }

    public ItemCreator setAbility(String trigger, String methodName, String title, Map<String, String> params) {
        // Step 1: Store the ability method in the PersistentDataContainer
        NamespacedKey key = new NamespacedKey(plugin, "ability_" + trigger);
        dataContainer.set(key, PersistentDataType.STRING, methodName);
    
        // Step 2: Extract cooldown from params (default to "0" if missing)
        String cooldown = params.getOrDefault("cooldown", "0");
    
        // Step 3: Fetch the lore template and replace placeholders
        String formattedLore = getFormattedLore(trigger, Map.of(
                "title", title,
                "cooldown", cooldown
        ));
        addLore(formattedLore);
    
        // Step 4: Append additional parameters dynamically (excluding 'method', 'title', and 'cooldown')
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            // if (!paramKey.equals("method") && !paramKey.equals("title") && !paramKey.equals("cooldown")) {
            //     String paramLore = getFormattedLore("ability_param_format", Map.of(
            //             "key", paramKey,
            //             "value", entry.getValue()
            //     ));
            //     addLore(paramLore);
            // }
    
            // Save each parameter in the PersistentDataContainer
            NamespacedKey paramContainerKey = new NamespacedKey(plugin, "ability_" + trigger + "_" + paramKey);
            dataContainer.set(paramContainerKey, PersistentDataType.STRING, entry.getValue());
        }
    
        return this;
    }
    
    
    public ItemCreator setAbilityParam(String paramName, String paramValue) {
        NamespacedKey key = new NamespacedKey(plugin, "itemAbility_" + paramName);
        dataContainer.set(key, PersistentDataType.STRING, paramValue);
        if (isDebuggerOn) {plugin.getLogger().info("Setting ability param: " + paramName + " with value: " + paramValue);}  // Log the parameter being set

        return this;
    }

    // Add enchantment with compatibility check
    public ItemCreator addEnchantment(Enchantment enchantment, int level) {
        Material itemType = itemStack.getType();
        if (isEnchantmentCompatible(enchantment, itemType)) {
            itemMeta.addEnchant(enchantment, level, true);
        } else {
            plugin.getLogger().info("Enchantment " + enchantment.getKey().getKey() + " is not compatible with " + itemType);
        }
        return this;
    }



    // Set unbreakable status for the item
    public ItemCreator setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        
        return this;
    }

    // Set temporary potion effect (for consumable items like potions or food)
    public ItemCreator setPotionEffect(PotionEffectType effectType, int duration, int amplifier) {
        if (itemMeta instanceof PotionMeta) {
            PotionMeta potionMeta = (PotionMeta) itemMeta;
            
            // Debugging statement to log the effect details being set
            if (isDebuggerOn) {plugin.getLogger().info("Setting potion effect: " + effectType.getName() +
                                    " | Duration: " + duration +
                                    " ticks | Amplifier: " + amplifier);}
            
            PotionEffect potionEffect = new PotionEffect(effectType, duration, amplifier);
            potionMeta.addCustomEffect(potionEffect, true);
            itemStack.setItemMeta(potionMeta);
        } else {
            plugin.getLogger().warning("Attempted to set potion effect on a non-potion item: " + itemStack.getType());
        }
        return this;
    }

    public ItemCreator setFoodEffect(PotionEffectType effectType, int duration, int amplifier) {
        NamespacedKey effectKey = new NamespacedKey(plugin, "effect");
        NamespacedKey durationKey = new NamespacedKey(plugin, "duration");
        NamespacedKey amplifierKey = new NamespacedKey(plugin, "amplifier");
    
        dataContainer.set(effectKey, PersistentDataType.STRING, effectType.getName().toLowerCase());
        dataContainer.set(durationKey, PersistentDataType.INTEGER, duration);
        dataContainer.set(amplifierKey, PersistentDataType.INTEGER, amplifier);
    
        // Add the custom potion effect lore to make it look like a potion
        addEffectLore(effectType.getName(), duration, amplifier);
    
        itemStack.setItemMeta(itemMeta); // Save metadata to the item
        return this;
    }
    
    public ItemCreator addEffectLore(String effectName, int duration, int amplifier) {
        // Format the effect to look like potion effect lore
        String durationString = String.format("%d:%02d", duration / 20 / 60, (duration / 20) % 60);
        String amplifierString = getRomanNumeral(amplifier + 1); // Potion levels start from I
        String effectLore = "§9" + effectName + " " + amplifierString + " (" + durationString + ")"; // §9 for blue text
    
        // Add this to the lore
        addLore(effectLore);
        return this;
    }
    
    // Helper method to convert integer to Roman numeral (for amplifiers)
    private String getRomanNumeral(int number) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 1 && number <= numerals.length) {
            return numerals[number - 1];
        }
        return String.valueOf(number); // Fallback to numeric if out of range
    }


    public ItemCreator setExplosionProof(boolean explosionProof) {
        dataContainer.set(new NamespacedKey(plugin, "explosionProof"), PersistentDataType.INTEGER, explosionProof ? 1 : 0);
        // addLore("§7Explosion Proof: " + (explosionProof ? "Yes" : "No"));
        addLore(getFormattedLore("explosionProof", Map.of("value", String.valueOf(explosionProof?"Yes":"No"))));

        return this;
    }
    
    // Set the ability and parameters to run on block placement
    public ItemCreator setPlaceAbility(String abilityName, Map<String, String> params) {
        dataContainer.set(new NamespacedKey(plugin, "placeAbility"), PersistentDataType.STRING, abilityName);
        // addLore("§7Ability on Place: " + abilityName);
        addLore(getFormattedLore("on-place", Map.of("value", String.valueOf(abilityName))));

        for (Map.Entry<String, String> entry : params.entrySet()) {
            dataContainer.set(new NamespacedKey(plugin, "placeAbility_" + entry.getKey()), PersistentDataType.STRING, entry.getValue());
        }
        return this;
    }
    
    // Set the ability and parameters to run on block break
    public ItemCreator setBreakAbility(String abilityName, Map<String, String> params) {
        dataContainer.set(new NamespacedKey(plugin, "breakAbility"), PersistentDataType.STRING, abilityName);
        // addLore("§7Ability on Break: " + abilityName);
        addLore(getFormattedLore("on-block-break", Map.of("value", String.valueOf(abilityName))));

        for (Map.Entry<String, String> entry : params.entrySet()) {
            dataContainer.set(new NamespacedKey(plugin, "breakAbility_" + entry.getKey()), PersistentDataType.STRING, entry.getValue());
        }
        return this;
    }
    
    // Set block to be destroyed after placement
    public ItemCreator setTemporaryOnPlace(boolean temporary) {
        dataContainer.set(new NamespacedKey(plugin, "temporaryOnPlace"), PersistentDataType.INTEGER, temporary ? 1 : 0);
        // if (temporary) addLore("§7Temporary Block: Destroys after use");
        addLore(getFormattedLore("temporary-block", Map.of("value", String.valueOf(""))));
        return this;
    }



    
    // Finalize the item creation
    public ItemStack create() {
        // Hide default attributes in the tooltip
        // Apply custom and default attribute modifiers
        applyModifiers();
        // itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    // Set attribute with compatibility check

    public ItemCreator setAttribute(Attribute attribute, double value, Operation operation, String equipSlotString) {
        EquipmentSlot equipSlot = autoDetectSlot(itemStack.getType());
        if (equipSlot == null) {
            plugin.getLogger().warning("Invalid EquipmentSlot: " + equipSlotString);
            return this;
        }

        if (isAttributeCompatible(attribute, itemStack.getType())) {
            NamespacedKey key = new NamespacedKey(plugin, attribute.name().toLowerCase() + "_modifier");
            AttributeModifier modifier = new AttributeModifier(key, value, operation, equipSlot.getGroup());

            // Replace any existing custom modifier for the same attribute
            attributeModifiers.removeAll(attribute);
            attributeModifiers.put(attribute, modifier);

            // Display in lore
            // addLore("§7" + formatAttributeName(attribute) + ": " + formatValue(value));
            String loreKey = attribute.name().toLowerCase();
            addLore(getFormattedLore(loreKey, Map.of("value", String.valueOf(value)))); 

        } else {
            plugin.getLogger().warning("Attribute " + attribute.name() + " is not compatible with " + itemStack.getType());
        }
        return this;
    }

    // Auto-detect slot based on material for "ANY" case
    private EquipmentSlot autoDetectSlot(Material material) {
        String itemName = material.name();
        if (itemName.endsWith("_HELMET")) {
            return EquipmentSlot.HEAD;
        } else if (itemName.endsWith("_CHESTPLATE")) {
            return EquipmentSlot.CHEST;
        } else if (itemName.endsWith("_LEGGINGS")) {
            return EquipmentSlot.LEGS;
        } else if (itemName.endsWith("_BOOTS")) {
            return EquipmentSlot.FEET;
        } else if (itemName.endsWith("_SWORD") || itemName.endsWith("_AXE") || itemName.endsWith("_PICKAXE") || 
                   itemName.endsWith("_SHOVEL") || itemName.endsWith("_HOE") || material==Material.BOW ||
                material==Material.CROSSBOW  || material == Material.MACE || material == Material.TRIDENT) {
            return EquipmentSlot.HAND;
        }
        return EquipmentSlot.HAND;  // If no match, return null or you might choose a default slot like HAND.
    }
    
    private String formatValue(double value) {
        return String.format("%.1f", value);
    }

    // Apply custom and default attribute modifiers, with custom overwriting defaults
    private void applyModifiers() {
        Multimap<Attribute, AttributeModifier> combinedModifiers = HashMultimap.create();

        // Step 1: Add default modifiers first
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> defaultModifiers = itemStack.getType().getDefaultAttributeModifiers(slot);
            for (Attribute attribute : defaultModifiers.keySet()) {
                if (!attributeModifiers.containsKey(attribute)) {
                    combinedModifiers.putAll(attribute, defaultModifiers.get(attribute));
                }
            }
        }

        // Step 2: Overwrite defaults with custom modifiers
        for (Attribute attribute : attributeModifiers.keySet()) {
            Collection<AttributeModifier> customModifiers = attributeModifiers.get(attribute);
            combinedModifiers.replaceValues(attribute, customModifiers);
        }

        itemMeta.setAttributeModifiers(combinedModifiers);  // Set combined modifiers on the item
    }

    
    private boolean isAttributeCompatible(Attribute attribute, Material itemType) {
        switch (attribute) {
            // Attack-related attributes (meant for weapons)
            case GENERIC_ATTACK_DAMAGE:
            case GENERIC_ATTACK_SPEED:
            case GENERIC_ATTACK_KNOCKBACK:
            case GENERIC_BURNING_TIME:
                // return itemType.name().endsWith("_SWORD") || 
                //     itemType.name().endsWith("_AXE") ||
                //     itemType.name().endsWith("_PICKAXE") ||
                //     itemType.name().endsWith("_SHOVEL") ||
                //     itemType.name().endsWith("_HOE") ||
                //     itemType == Material.TRIDENT || 
                //     itemType == Material.MACE || 
                //     itemType == Material.BOW || 
                //     itemType == Material.CROSSBOW;
    
            // Armor-related attributes
            case GENERIC_ARMOR:
            case GENERIC_ARMOR_TOUGHNESS:
                // return itemType.name().endsWith("_HELMET") ||
                //        itemType.name().endsWith("_CHESTPLATE") ||
                //        itemType.name().endsWith("_LEGGINGS") ||
                //        itemType.name().endsWith("_BOOTS");

            case GENERIC_KNOCKBACK_RESISTANCE:
            case GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE:
            case GENERIC_FALL_DAMAGE_MULTIPLIER:
            case GENERIC_FLYING_SPEED:
            case GENERIC_MAX_ABSORPTION:
            case GENERIC_GRAVITY:
            case GENERIC_JUMP_STRENGTH:
            case GENERIC_MOVEMENT_EFFICIENCY:
            case GENERIC_OXYGEN_BONUS:
            case GENERIC_SAFE_FALL_DISTANCE:
            case GENERIC_STEP_HEIGHT:
            case GENERIC_WATER_MOVEMENT_EFFICIENCY:
            case GENERIC_MAX_HEALTH:
            case GENERIC_LUCK:
            case GENERIC_MOVEMENT_SPEED:
            case GENERIC_SCALE:
                return true;
            default:
                return false;
        }
    }

    private String formatAttributeName(Attribute attribute) {
        switch (attribute) {
            case GENERIC_ATTACK_DAMAGE:
                return "Attack Damage";
            case GENERIC_ATTACK_SPEED:
                return "Attack Speed";
            case GENERIC_ARMOR:
                return "Armor";
            case GENERIC_ARMOR_TOUGHNESS:
                return "Armor Toughness";
            case GENERIC_KNOCKBACK_RESISTANCE:
                return "Knockback Resistance";
            case GENERIC_MAX_HEALTH:
                return "Max Health";
            case GENERIC_LUCK:
                return "Luck";
            case GENERIC_MOVEMENT_SPEED:
                return "Movement Speed";
            default:
                return attribute.name().replace("GENERIC_", "").replace("_", " ");
        }
    }
    
    // Compatibility check for enchantments based on item type
    private boolean isEnchantmentCompatible(Enchantment enchantment, Material itemType) {
        switch (enchantment.getKey().getKey()) {
            case "sharpness":
            case "smite":
            case "bane_of_arthropods":
            case "sweeping_edge":
                return itemType.name().endsWith("_SWORD");

            case "efficiency":
            case "silk_touch":
            case "fortune":
                return itemType.name().endsWith("_PICKAXE") ||
                       itemType.name().endsWith("_AXE") ||
                       itemType.name().endsWith("_SHOVEL") ||
                       itemType == Material.SHEARS;

            case "power":
            case "punch":
            case "flame":
            case "infinity":
                return itemType == Material.BOW;

            case "protection":
            case "fire_protection":
            case "blast_protection":
            case "projectile_protection":
            case "thorns":
                return itemType.name().endsWith("_HELMET") ||
                       itemType.name().endsWith("_CHESTPLATE") ||
                       itemType.name().endsWith("_LEGGINGS") ||
                       itemType.name().endsWith("_BOOTS");

            case "aqua_affinity":
            case "respiration":
                return itemType.name().endsWith("_HELMET");

            case "depth_strider":
            case "frost_walker":
                return itemType.name().endsWith("_BOOTS");

            case "looting":
            case "knockback":
            case "fire_aspect":
                return itemType.name().endsWith("_SWORD");

            case "luck_of_the_sea":
            case "lure":
                return itemType == Material.FISHING_ROD;

            case "channeling":
            case "riptide":
            case "loyalty":
            case "impaling":
                return itemType == Material.TRIDENT;

            case "soul_speed":
                return itemType.name().endsWith("_BOOTS");

            case "quick_charge":
            case "piercing":
            case "multishot":
                return itemType == Material.CROSSBOW;

            case "unbreaking":
            case "mending":
            case "vanishing_curse":
                return true;

            default:
                return false;
        }
    }



    // Helper method to fetch and format lore templates dynamically
    public String getFormattedLore(String templateKey, Map<String, String> replacements) {
        if (isDebuggerOn) {plugin.getLogger().info("Fetching lore template for key: " + templateKey);}
        String template = loreTemplates.getOrDefault(templateKey, "&7" + templateKey + ": {value}");
        if (isDebuggerOn) {plugin.getLogger().info("Template found: " + template);}
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return translateColorCodes(template);
    }
    

    // Add lore for displaying custom attributes
    public ItemCreator addLore(String line) {
        List<String> lore = itemMeta.getLore();
        if (lore == null) lore = new ArrayList<>();
        lore.add(translateColorCodes(line)); // Convert color codes
        itemMeta.setLore(lore);
        return this;
    }
    /**
     * Converts color codes using '&' to Minecraft-compatible '§' color codes.
     *
     * @param text The string containing '&' color codes.
     * @return The formatted string with '§' color codes.
     */
    public String translateColorCodes(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }

    

}
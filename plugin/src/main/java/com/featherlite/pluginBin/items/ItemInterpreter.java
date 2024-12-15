package com.featherlite.pluginBin.items;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemInterpreter {
    private final JavaPlugin plugin;

    public ItemInterpreter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if an item has a specific custom data key.
     */
    public boolean hasData(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(new NamespacedKey(plugin, key), PersistentDataType.STRING) ||
               container.has(new NamespacedKey(plugin, key), PersistentDataType.INTEGER);
    }

    /**
     * Retrieve the category of the item.
     */
    public String getItemCategory(ItemStack item) {
        return getStringData(item, "itemCategory");
    }

    /**
     * Retrieve the right-click ability assigned to the item.
     */
    public String getRightClickAbility(ItemStack item) {
        return getStringData(item, "rightClickAbility");
    }

    /**
     * Retrieve the attack damage attribute for weapons.
     */
    public Double getAttackDamage(ItemStack item) {
        return getDoubleData(item, "attackDamage");
    }

    /**
     * Retrieve the attack speed attribute for weapons.
     */
    public Double getAttackSpeed(ItemStack item) {
        return getDoubleData(item, "attackSpeed");
    }

    /**
     * Retrieve the armor value for armor items.
     */
    public Double getArmor(ItemStack item) {
        return getDoubleData(item, "armor");
    }

    /**
     * Retrieve the movement speed modifier for items.
     */
    public Double getMovementSpeed(ItemStack item) {
        return getDoubleData(item, "movementSpeed");
    }

    /**
     * Retrieve custom potion effects applied to the item.
     */
    public List<PotionEffectData> getPotionEffects(ItemStack item) {
        List<PotionEffectData> effects = new ArrayList<>();
        
        for (String key : getKeysWithPrefix(item, "potionEffect_")) {
            String effectData = getStringData(item, key);
            if (effectData != null) {
                String[] parts = effectData.split(":");
                if (parts.length == 3) {
                    String effectType = parts[0];
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    effects.add(new PotionEffectData(effectType, duration, amplifier));
                }
            }
        }
        
        return effects;
    }

    /**
     * Retrieve all keys with a specified prefix in the item's PersistentDataContainer.
     */
    private List<String> getKeysWithPrefix(ItemStack item, String prefix) {
        List<String> keys = new ArrayList<>();
        
        if (item != null && item.hasItemMeta()) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            container.getKeys().forEach(key -> {
                if (key.getKey().startsWith(prefix)) {
                    keys.add(key.getKey());
                }
            });
        }
        
        return keys;
    }

    /**
     * Helper method to retrieve a custom string data.
     */
    private String getStringData(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return container.get(new NamespacedKey(plugin, key), PersistentDataType.STRING);
    }

    /**
     * Helper method to retrieve a custom double data.
     */
    private Double getDoubleData(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        Integer intData = container.get(new NamespacedKey(plugin, key), PersistentDataType.INTEGER);
        return intData != null ? intData.doubleValue() : null;
    }

    /**
     * Retrieve health restoration value for food items.
     */
    public Double getRestoreHealth(ItemStack item) {
        return getDoubleData(item, "restoreHealth");
    }

    /**
     * Retrieve saturation restoration value for food items.
     */
    public Double getRestoreSaturation(ItemStack item) {
        return getDoubleData(item, "restoreSaturation");
    }

    /**
     * Represents potion effect data with type, duration, and amplifier.
     */
    public static class PotionEffectData {
        private final String effectType;
        private final int duration;
        private final int amplifier;

        public PotionEffectData(String effectType, int duration, int amplifier) {
            this.effectType = effectType;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public String getEffectType() {
            return effectType;
        }

        public int getDuration() {
            return duration;
        }

        public int getAmplifier() {
            return amplifier;
        }

        @Override
        public String toString() {
            return effectType + " (Duration: " + duration + ", Amplifier: " + amplifier + ")";
        }
    }
}

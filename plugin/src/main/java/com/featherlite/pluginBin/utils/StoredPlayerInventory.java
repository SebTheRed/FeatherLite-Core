package com.featherlite.pluginBin.utils;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

public class StoredPlayerInventory implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String serializedContents;
    private final String serializedArmorContents;

    public StoredPlayerInventory(ItemStack[] contents, ItemStack[] armorContents) {
        this.serializedContents = serializeItemStackArray(contents);
        this.serializedArmorContents = serializeItemStackArray(armorContents);
    }

    public ItemStack[] getContents() {
        return deserializeItemStackArray(serializedContents);
    }

    public ItemStack[] getArmorContents() {
        return deserializeItemStackArray(serializedArmorContents);
    }

    private String serializeItemStackArray(ItemStack[] items) {
        try {
            return BukkitObjectUtils.toBase64(items);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack array", e);
        }
    }

    private ItemStack[] deserializeItemStackArray(String data) {
        try {
            return BukkitObjectUtils.fromBase64(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize ItemStack array", e);
        }
    }
}

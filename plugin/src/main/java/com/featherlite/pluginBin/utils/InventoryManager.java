package com.featherlite.pluginBin.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryManager {
    private final JavaPlugin plugin;
    private final Map<UUID, StoredPlayerInventory> inventoryMap = new HashMap<>();
    private final File storageFolder;

    public InventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFolder = new File(plugin.getDataFolder(), "inventories");
        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }
    }

    // Save inventory to memory and file
    public void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // Store inventory in memory
        StoredPlayerInventory storedInventory = new StoredPlayerInventory(
                player.getInventory().getContents(),
                player.getInventory().getArmorContents()
        );
        inventoryMap.put(uuid, storedInventory);

        // Save inventory to file
        saveInventoryToFile(uuid, storedInventory);

        // Clear player's inventory
        player.getInventory().clear();
    }

    // Restore inventory from memory or file
    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();
        StoredPlayerInventory storedInventory = inventoryMap.get(uuid);

        if (storedInventory == null) {
            // If not in memory, try loading from file
            storedInventory = loadInventoryFromFile(uuid);
        }

        if (storedInventory != null) {
            player.getInventory().setContents(storedInventory.getContents());
            player.getInventory().setArmorContents(storedInventory.getArmorContents());

            // Cleanup storage
            inventoryMap.remove(uuid);
            deleteInventoryFile(uuid);
        } else {
            player.sendMessage("§cFailed to restore your inventory. Please contact an admin!");
            Bukkit.getLogger().warning("Could not restore inventory for player: " + player.getName());
        }
    }


    /**
     * Checks if the player has a saved inventory.
     *
     * @param player The player to check.
     * @return True if a saved inventory exists, false otherwise.
     */
    public boolean hasStoredInventory(Player player) {
        UUID uuid = player.getUniqueId();
        File inventoryFile = new File(storageFolder, uuid.toString() + ".dat");
        return inventoryFile.exists();
    }

    // Save inventory to file
    private void saveInventoryToFile(UUID uuid, StoredPlayerInventory inventory) {
        File file = new File(storageFolder, uuid.toString() + ".dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(inventory);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save inventory for player: " + uuid);
            e.printStackTrace();
        }
    }

    // Load inventory from file
    private StoredPlayerInventory loadInventoryFromFile(UUID uuid) {
        File file = new File(storageFolder, uuid.toString() + ".dat");
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (StoredPlayerInventory) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().severe("Failed to load inventory for player: " + uuid);
            e.printStackTrace();
            return null;
        }
    }

    // Delete inventory file
    private void deleteInventoryFile(UUID uuid) {
        File file = new File(storageFolder, uuid.toString() + ".dat");
        if (file.exists()) {
            file.delete();
        }
    }
}


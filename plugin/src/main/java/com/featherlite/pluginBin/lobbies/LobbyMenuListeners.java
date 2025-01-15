package com.featherlite.pluginBin.lobbies;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyMenuListeners implements Listener {
    private final InstanceManager instanceManager;
    private final TeamSelectorBook teamSelectorBook;

    public LobbyMenuListeners(JavaPlugin plugin, InstanceManager instanceManager, TeamSelectorBook teamSelectorBook) {
        this.instanceManager = instanceManager;
        this.teamSelectorBook = teamSelectorBook;
    }

    /**
     * Handle when a player interacts with the Team Selector book.
     *
     * @param event The player interaction event.
     */
    @EventHandler
    public void onBookClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        try {
            // Check if the player is holding the "Team Selector" book
            if (item == null || !item.getType().equals(Material.BOOK)) {
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals("§aTeam Selector")) {
                return;
            }

            // Fetch the GameInstance for the player
            GameInstance instance = instanceManager.getInstanceForPlayer(player);
            if (instance == null) {
                player.sendMessage("§cYou are not in a game instance. ?? Report error to admin. Team Selector book instance == null");
                Bukkit.getLogger().warning("Player " + player.getName() + " tried to use the Team Selector book, but they are not in a game instance.");
                return;
            }

            // Open the team selection GUI
            teamSelectorBook.openTeamSelectionGUI(player, instance);
            event.setCancelled(true); // Prevent default item interaction

        } catch (Exception e) {
            // Log unexpected errors
            Bukkit.getLogger().severe("An error occurred while handling Team Selector book click for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while processing your action. Please report this to the admin.");
        }
    }


@EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getView().getTopInventory();
        if (clickedInventory == null || event.getClickedInventory() == null || event.getClickedInventory() != clickedInventory) {
            return; // Only handle clicks in the top inventory (GUI)
        }

        String title = event.getView().getTitle();
        switch (title) {
            case "§6Team Selection":
                teamSelectorBook.handleTeamSelectionClick(event, instanceManager);
                break;

            // Add more cases here for other GUIs as needed
            case "§6Example Menu":
                // handleExampleMenuClick(event);
                break;

            default:
                break; // Unknown GUI, do nothing
        }
    }

    
}

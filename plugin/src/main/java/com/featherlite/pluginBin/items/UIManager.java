package com.featherlite.pluginBin.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class UIManager implements Listener {
    private final ItemManager itemManager;
    private final Map<String, Map<Integer, Inventory>> categoryInventories = new HashMap<>(); // category -> (page -> inventory)
    private static final int ITEMS_PER_PAGE = 45; // 45 slots for items (6 rows x 9 slots - 1 row for navigation)

    public UIManager(JavaPlugin plugin, ItemManager itemManager) {
        this.itemManager = itemManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the main UI showing all item categories.
     */
    public void openItemUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Item Categories");

        for (String category : itemManager.getCategories()) {
            ItemStack categoryItem = itemManager.getCategoryIcon(category);
            if (categoryItem.getType() != Material.BARRIER) {
                // Add item count to the category icon's display name
                ItemMeta meta = categoryItem.getItemMeta();
                int itemCount = itemManager.getItemsInCategory(category).size();
                meta.setDisplayName("§e" + category + " (" + itemCount + " items)");
                categoryItem.setItemMeta(meta);

                inventory.addItem(categoryItem);
            }
        }
        
        player.openInventory(inventory);
    }

    /**
     * Opens a specific category UI with the items from that category and handles pagination.
     */
    public void openCategoryUI(Player player, String category, int page) {
        Map<Integer, Inventory> categoryPages = categoryInventories.computeIfAbsent(category, key -> new HashMap<>());
        
        // If the inventory for the specified page doesn't exist, create it
        Inventory inventory = categoryPages.computeIfAbsent(page, key -> createCategoryInventory(category, page));
        
        player.openInventory(inventory);
    }

    /**
     * Creates an inventory for a specific category page.
     */
    private Inventory createCategoryInventory(String category, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Items - " + category + " - Page " + (page + 1));

        List<ItemStack> items = List.copyOf(itemManager.getItemsInCategory(category).values());
        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        for (int i = start; i < end; i++) {
            inventory.addItem(items.get(i));
        }

        // Navigation items
        if (page > 0) {
            inventory.setItem(45, createNavigationItem(Material.ARROW, "§aPrevious Page"));
        }
        if (end < items.size()) {
            inventory.setItem(53, createNavigationItem(Material.ARROW, "§aNext Page"));
        }
        inventory.setItem(49, createNavigationItem(Material.BARRIER, "§cBack to Categories"));

        return inventory;
    }

    public void clearCategoryInventories() {
        categoryInventories.clear();
    }
    /**
     * Helper method to create a navigation item.
     */
    private ItemStack createNavigationItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        Player player = (Player) event.getWhoClicked();


        String inventoryTitle = event.getView().getTitle();

        // Handle clicks in the "Item Categories" inventory
        if (inventoryTitle.equals("Item Categories")) {
            event.setCancelled(true);
            if (clickedItem.getType() != Material.BARRIER) {
                String categoryName = clickedItem.getItemMeta().getDisplayName().replaceAll("§e", "").replaceAll("\\s*\\(\\d+ items\\)", ""); // Remove color codes and item count
                openCategoryUI(player, categoryName, 0); // Open category at page 0
            }
        }
        // Handle clicks within a specific category's inventory
        else if (inventoryTitle.contains("Items - ")) {
            event.setCancelled(true);

            // Extract category and page from the inventory title
            String[] titleParts = inventoryTitle.split(" - Page ");
            if (titleParts.length < 2) return;

            String category = titleParts[0];
            int page = Integer.parseInt(titleParts[1]) - 1;

            // Check if the player clicked on a navigation item
            String clickedName = clickedItem.getItemMeta().getDisplayName();
            if (clickedName.equals("§aPrevious Page")) {
                openCategoryUI(player, category, page - 1);
            } else if (clickedName.equals("§aNext Page")) {
                openCategoryUI(player, category, page + 1);
            } else if (clickedName.equals("§cBack to Categories")) {
                openItemUI(player);
            } else {
                // Handle item retrieval
                player.getInventory().addItem(clickedItem.clone());
                player.sendMessage("You received: " + clickedItem.getItemMeta().getDisplayName());
            }
        }
    }
}

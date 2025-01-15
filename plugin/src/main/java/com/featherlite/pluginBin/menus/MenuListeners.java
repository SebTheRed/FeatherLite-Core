package com.featherlite.pluginBin.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class MenuListeners implements Listener {
    private final MenuManager menuManager;
    private final JavaPlugin plugin;
    private final boolean isDebuggerOn;

    public MenuListeners(JavaPlugin plugin, MenuManager menuManager, boolean isDebuggerOn) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.isDebuggerOn = isDebuggerOn;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // plugin.getLogger().info("Clicked inside of an inventory.");
        // Ensure the click is in a valid inventory
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
    
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || event.getCurrentItem() == null) {
            return;
        }
    
        // Get the menu ID using the inventory title
        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());
        String menuId = menuManager.getInventoryTitleToMenuId().get(inventoryTitle);
        if (isDebuggerOn) {plugin.getLogger().info("inventoryTitle: " + inventoryTitle);}
        if (isDebuggerOn) {plugin.getLogger().info("menuId" + menuId);}
        if (menuId == null) {
            // plugin.getLogger().warning("Not a managed menu!");
            return; // Not a managed menu
        }
    
        // Get the corresponding menu
        Menu menu = menuManager.getMenus().get(menuId);
        if (menu == null) {
            plugin.getLogger().warning("No menu found with ID: " + menuId);
            return; // Menu not found
        }
    
        // Cancel the click to prevent item movement
        event.setCancelled(true);
        // Find the page with a matching title
        int slot = event.getSlot();
        MenuPage page = menu.getPageByTitle(inventoryTitle);

        MenuButton button = page.getItem(slot);
        if (button == null) {
            plugin.getLogger().warning("No button found at slot: " + slot);
            return;
        }
        
        if (isDebuggerOn) {plugin.getLogger().info("Executing button action for button type: " + button.getClass().getSimpleName());}
        button.onClick(player);
    }
    
}


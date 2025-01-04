package com.featherlite.pluginBin.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RouteButton implements MenuButton {
    private final ItemStack icon;
    private final String targetPage;
    private final MenuManager menuManager; // Store MenuManager as a field

    public RouteButton(ItemStack icon, String targetPage, MenuManager menuManager) {
        this.icon = icon;
        this.targetPage = targetPage;
        this.menuManager = menuManager;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public void onClick(Player player) {
        if (targetPage == null || targetPage.isEmpty()) {
            player.sendMessage("§cTarget page not set for this button.");
            return;
        }

        String inventoryTitle = player.getOpenInventory().getTitle();
        Menu menu = menuManager.getMenus().get(inventoryTitle);

        if (menu == null) {
            player.sendMessage("§cThe current menu could not be found.");
            return;
        }

        if (!menu.hasPage(targetPage)) {
            player.sendMessage("§cThe target page does not exist.");
            return;
        }

        MenuPage target = menu.getPage(targetPage);
        String menuPageTitle = target.getTitle();
        player.openInventory(menuManager.buildInventory(target, menu.getSlots(), menu.getID(), menuPageTitle));
    }
}

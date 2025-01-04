package com.featherlite.pluginBin.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface MenuButton {
    // Returns the visual representation of the button
    ItemStack getIcon();

    // Executes the button's action when clicked
    void onClick(Player player);
}
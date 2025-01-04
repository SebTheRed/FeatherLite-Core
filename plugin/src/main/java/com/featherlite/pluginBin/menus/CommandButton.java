package com.featherlite.pluginBin.menus;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandButton implements MenuButton {
    private final ItemStack icon;
    private final String command;

    public CommandButton(ItemStack icon, String command) {
        this.icon = icon;
        this.command = command;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public void onClick(Player player) {
        if (command != null && !command.isEmpty()) {
            // Run the command as the player
            player.performCommand(command);
        }
    }
}

package com.featherlite.pluginBin.commands;

import com.featherlite.pluginBin.menus.Menu;
import com.featherlite.pluginBin.menus.MenuManager;
import com.featherlite.pluginBin.menus.MenuPage;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MenuCommands implements TabCompleter {
    private final MenuManager menuManager;
    private final JavaPlugin plugin;

    public MenuCommands(MenuManager menuManager, JavaPlugin plugin) {
        this.menuManager = menuManager;
        this.plugin = plugin;
    }

    public boolean handleMenuCommands(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("§cUsage: /menu <menu_id>");
            return true;
        }

        String menuId = args[0];
        Menu menu = menuManager.getMenus().get(menuId);

        if (menu == null) {
            player.sendMessage("§cMenu with ID '" + menuId + "' does not exist.");
            return true;
        }

        // Open the main page of the menu
        MenuPage target = menu.getPage("main");
        String menuPageTitle = target.getTitle();
        player.openInventory(menuManager.buildInventory(menu.getPage("main"), menu.getSlots(), menu.getID(), menuPageTitle));
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(menuManager.getMenus().keySet());
        }
        return new ArrayList<>();
    }
}

package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import com.featherlite.pluginBin.lobbies.GamesManager.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Set;

public class GamesUI implements Listener {

    private final GamesManager gamesManager;
    private final InstanceManager instanceManager;

    public GamesUI(GamesManager gamesManager, InstanceManager instanceManager) {
        this.gamesManager = gamesManager;
        this.instanceManager = instanceManager;
    }


    /**
     * Handles menu interactions.
     * @param event The inventory click event.
     */
    @EventHandler
    public void handleMenuClick(InventoryClickEvent event) {
        // Check if a player triggered the event
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();

        // Check if the inventory belongs to GamesUI
        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());
        if (!isGamesUIMenu(inventoryTitle)) {
            return; // Exit if this isn't one of your menus
        }

        // Get the clicked item
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) return;

        // Get the display name of the clicked item
        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        // Handle specific menu options
        event.setCancelled(true); // Cancel the click event to prevent moving items
        switch (displayName) {
            case "View/Join Games":
                openViewJoinGamesMenu(player);
                break;

            case "Create Game":
                openCreateGamesMenu(player);
                break;

            case "Select Map":
                openMapSelectionMenu(player);
                break;

            case "Toggle Open/Closed":
                toggleOpenClosed(player);
                break;

            default:
                break;
        }
    }


    /**
     * Opens the main menu for the player.
     *
     * @param player The player opening the menu.
     */
    public void openMainMenu(Player player) {
        Inventory mainMenu = Bukkit.createInventory(null, 27, ChatColor.GREEN + "Games Menu");

        // Option 1: View/Join Games
        ItemStack viewJoinGames = createMenuItem("View/Join Games", ChatColor.BLUE + "Click to view or join games.");
        mainMenu.setItem(12, viewJoinGames);

        // Option 2: Create Game
        ItemStack createGame = createMenuItem("Create Game", ChatColor.LIGHT_PURPLE + "Click to create a new game.");
        mainMenu.setItem(14, createGame);

        player.openInventory(mainMenu);
    }

    /**
     * Opens the View/Join Games menu.
     *
     * @param player The player opening the menu.
     */
    public void openViewJoinGamesMenu(Player player) {
        Set<String> gameTypes = gamesManager.getAllGameTypes();

        // Create paginated menus if necessary
        int inventorySize = 54;
        Inventory viewJoinMenu = Bukkit.createInventory(null, inventorySize, ChatColor.BLUE + "View/Join Games");

        // Populate menu with active games
        int slot = 10;
        for (String type : gameTypes) {
            ItemStack gameItem = createGameTypeItem(type);
            viewJoinMenu.setItem(slot++, gameItem);
        }

        player.openInventory(viewJoinMenu);
    }


        /**
     * Opens the View/Join Games menu.
     *
     * @param player The player opening the menu.
     */
    public void openCreateGamesMenu(Player player) {
        Set<String> gameTypes = gamesManager.getAllGameTypes();

        // Create paginated menus if necessary
        int inventorySize = 54;
        Inventory viewJoinMenu = Bukkit.createInventory(null, inventorySize, ChatColor.GREEN + "Create a Game");

        // Populate menu with active games
        int slot = 10;
        for (String type : gameTypes) {
            ItemStack gameItem = createGameTypeItem(type);
            viewJoinMenu.setItem(slot++, gameItem);
        }

        player.openInventory(viewJoinMenu);
    }

    /**
     * Opens the Create Game menu.
     *
     * @param player The player opening the menu.
     */
    public void openCreateGameMenu(Player player) {
        Inventory createMenu = Bukkit.createInventory(null, 27, ChatColor.LIGHT_PURPLE + "Create Game");

        // Option 1: Select Map
        ItemStack selectMap = createMenuItem("Select Map", ChatColor.YELLOW + "Click to choose a map.");
        createMenu.setItem(3, selectMap);

        // Option 2: Open/Close Game
        ItemStack toggleOpenClose = createMenuItem("Toggle Open/Closed", ChatColor.YELLOW + "Set the game to open or closed.");
        createMenu.setItem(5, toggleOpenClose);

        player.openInventory(createMenu);
    }



    /**
     * Creates a menu item.
     *
     * @param name        The display name of the item.
     * @param description The description of the item.
     * @return The ItemStack representing the menu item.
     */
    private ItemStack createMenuItem(String name, String description) {
        ItemStack item = new ItemStack(org.bukkit.Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + name);
            List<String> lore = new ArrayList<>();
            lore.add(description);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a game item for an active instance.
     *
     * @param instance The game instance.
     * @return The ItemStack representing the game.
     */
    private ItemStack createGameItem(GameInstance instance) {
        ItemStack item = new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + instance.getGameName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "World: " + instance.getWorldName());
            lore.add(ChatColor.GRAY + "Players: " + instance.getTotalPlayerCount() + "/" + instance.getTeamSizes().values().stream().mapToInt(Integer::intValue).sum());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Opens the map selection menu.
     *
     * @param player The player opening the menu.
     */
    private void openMapSelectionMenu(Player player) {
        List<String> availableMaps = gamesManager.listRegisteredGames().stream()
                .flatMap(gameData -> gameData.getWorldOptions().stream())
                .toList();

        Inventory mapMenu = Bukkit.createInventory(null, 54, ChatColor.YELLOW + "Select Map");
        for (int i = 0; i < availableMaps.size(); i++) {
            String mapName = availableMaps.get(i);
            ItemStack mapItem = createMenuItem(mapName, ChatColor.GRAY + "Click to select this map.");
            mapMenu.setItem(i, mapItem);
        }

        player.openInventory(mapMenu);
    }

    /**
     * Toggles the open/closed state of a game for the player.
     *
     * @param player The player toggling the game state.
     */
    private void toggleOpenClosed(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Toggle open/closed functionality not yet implemented.");
    }

    private boolean isGamesUIMenu(String title) {
        return title.equals("Games Menu") ||
               title.equals("View/Join Games") ||
               title.equals("Create Game") ||
               title.equals("Select Map");
    }

    /**
     * Creates an ItemStack to represent a game type.
     *
     * @param gameData The game type's metadata.
     * @return The ItemStack representing the game type.
     */
    private ItemStack createGameTypeItem(String gameType) {
        Material material;
        ChatColor gameColor;
        if (gameType.toLowerCase() == "bedwars") {
            material = Material.RED_BED;
            gameColor = ChatColor.RED;
        } else {
            material = Material.BOOK;
            gameColor = ChatColor.AQUA;
        }
        ItemStack item = new ItemStack(material); // Using BOOK as a placeholder item

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(gameColor + gameType);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to select this game type.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

}

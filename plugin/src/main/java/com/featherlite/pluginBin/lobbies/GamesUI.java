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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class GamesUI implements Listener {

    private final GamesManager gamesManager;
    private final InstanceManager instanceManager;
    private final Map<Player, String> chosenRegisteredGame = new HashMap<>();
    private final Map<Player, Boolean> isInstancePublic = new HashMap<>();
    private final Map<Player, String> selectedWorld = new HashMap<>();


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
        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        String itemDisplayName = ChatColor.stripColor(clickedItemMeta.getDisplayName());


        List<String> lore = clickedItemMeta.getLore();
        boolean loreContainsGameTypeString = false;
        if (lore != null) {loreContainsGameTypeString = lore.stream().anyMatch(line -> ChatColor.stripColor(line).contains("Game Type"));}
        boolean loreContainsRegisteredString = false;
        if (lore != null) {loreContainsRegisteredString = lore.stream().anyMatch(line -> ChatColor.stripColor(line).contains("Registered Game"));}
        boolean loreContainsJoinString = false;
        if (lore != null) {loreContainsJoinString = lore.stream().anyMatch(line -> ChatColor.stripColor(line).contains("Click to join this game."));}

        // Handle specific menu options
        event.setCancelled(true); // Cancel the click event to prevent moving items
        switch (inventoryTitle) {
            case "Games Menu":
                if (itemDisplayName.equalsIgnoreCase("View/Join Games")) {
                    openViewJoinGamesMenu(player);
                } else if (itemDisplayName.equalsIgnoreCase("Create Game")) {
                    openCreateGamesMenu(player);
                }
                break;
            case "View/Join Games":
                if (loreContainsGameTypeString) {
                    openViewRegisteredGameSelectionMenu(player, itemDisplayName);
                } else if (loreContainsRegisteredString) {
                    chosenRegisteredGame.put(player,itemDisplayName);
                    openViewInstancesSelectionMenu(player, chosenRegisteredGame.get(player));
                }
                break;

            case "Create a Game":
                if (loreContainsGameTypeString) {
                    openCreateRegisteredGameSelectionMenu(player, itemDisplayName);
                } else if (loreContainsRegisteredString) {
                    chosenRegisteredGame.put(player,itemDisplayName);
                    openCreateGameMenu(player, chosenRegisteredGame.get(player));
                }
                break;
            default:
                if (inventoryTitle.equalsIgnoreCase(chosenRegisteredGame.get(player))) {
                    if (loreContainsJoinString) {
                        player.sendMessage("Attempting to join: " + chosenRegisteredGame.get(player));
                    }
                } else if (inventoryTitle.equalsIgnoreCase("Create " + chosenRegisteredGame.get(player))) {
                    if (lore != null && lore.contains(ChatColor.GRAY + "Click to select this world.")) {
                        // Select world
                        selectedWorld.put(player, itemDisplayName);
                        player.sendMessage(ChatColor.GREEN + "Selected world: " + selectedWorld.get(player));
                        openCreateGameMenu(player, chosenRegisteredGame.get(player)); // Refresh the menu
                    } else if (itemDisplayName.equalsIgnoreCase("Open to Public") || itemDisplayName.equalsIgnoreCase("Closed to Private")) {
                        boolean isPublic = isInstancePublic.getOrDefault(player, false);
                        isInstancePublic.put(player, !isPublic); // Toggle state
                        player.sendMessage(ChatColor.YELLOW + "Instance is now " + (!isPublic ? "public" : "private") + ".");
                        openCreateGameMenu(player, chosenRegisteredGame.get(player)); // Refresh the menu
                    } else if (itemDisplayName.equalsIgnoreCase("Create Instance")) {
                        String chosenWorld = selectedWorld.get(player);
                        if (chosenWorld == null) {
                            player.sendMessage(ChatColor.RED + "You must select a world before creating an instance.");
                            return;
                        }
                
                        GameInstance instance = gamesManager.startGameInstance(
                            chosenRegisteredGame.get(player), chosenWorld, isInstancePublic.get(player) ,instanceManager
                        );
                
                        if (instance != null) {
                            player.sendMessage(ChatColor.GREEN + "Instance created successfully: " + instance.getGameName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Failed to create the instance.");
                        }
                    }
                }
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
        Inventory viewJoinMenu = Bukkit.createInventory(null, inventorySize, ChatColor.DARK_GREEN + "Create a Game");

        // Populate menu with active games
        int slot = 10;
        for (String type : gameTypes) {
            ItemStack gameItem = createGameTypeItem(type);
            viewJoinMenu.setItem(slot++, gameItem);
        }

        player.openInventory(viewJoinMenu);
    }


    public void openViewRegisteredGameSelectionMenu(Player player, String gameType) {
        List<GameData> registeredGames = gamesManager.getGamesByType(gameType);

        // Create paginated menus if necessary
        int inventorySize = 54;
        Inventory gameSelectionMenu = Bukkit.createInventory(null, inventorySize, ChatColor.BLUE + "View/Join Games");

        // Populate menu with active games
        int slot = 10;
        for (GameData game : registeredGames) {
            ItemStack gameItem = createGameItem(game);
            gameSelectionMenu.setItem(slot++, gameItem);
        }

        player.openInventory(gameSelectionMenu);
    }

    public void openViewInstancesSelectionMenu(Player player, String registeredGameName) {
        List<GameInstance> activeInstances = instanceManager.getInstancesByRegisteredGame(registeredGameName);
    
        int inventorySize = Math.min((activeInstances.size() / 18 + 1) * 18, 54); // Ensure rows fit within inventory limits
        Inventory activeGamesMenu = Bukkit.createInventory(null, inventorySize, ChatColor.BLUE + registeredGameName);
    
        int slot = 0;
        for (GameInstance instance : activeInstances) {
            ItemStack instanceItem = createInstanceItem(instance);
            activeGamesMenu.setItem(slot++, instanceItem);
        }
    
        player.openInventory(activeGamesMenu);
    }
    


    public void openCreateRegisteredGameSelectionMenu(Player player, String gameType) {
        List<GameData> registeredGames = gamesManager.getGamesByType(gameType);

        // Create paginated menus if necessary
        int inventorySize = 54;
        Inventory gameSelectionMenu = Bukkit.createInventory(null, inventorySize, ChatColor.DARK_GREEN + "Create a Game");

        // Populate menu with active games
        int slot = 10;
        for (GameData game : registeredGames) {
            ItemStack gameItem = createGameItem(game);
            gameSelectionMenu.setItem(slot++, gameItem);
        }

        player.openInventory(gameSelectionMenu);
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
               title.equals("Create a Game") ||
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
        if (gameType.toLowerCase().equals("bedwars")) {
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
            lore.add(ChatColor.DARK_GRAY + "Game Type");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an ItemStack to represent a game type.
     *
     * @param gameData The game type's metadata.
     * @return The ItemStack representing the game type.
     */
    private ItemStack createGameItem(GameData gameData) {
        ItemStack item = new ItemStack(org.bukkit.Material.BOOK); // Using BOOK as a placeholder item
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + gameData.getGameName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Type: " + gameData.getGameType());
            lore.add(ChatColor.GRAY + "Description: " + gameData.getDescription());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to select this game.");
            lore.add(ChatColor.DARK_GRAY + "Registered Game");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates an ItemStack to represent a game type.
     *
     * @param instance The game type's metadata.
     * @return The ItemStack representing the game type.
     */
    private ItemStack createInstanceItem(GameInstance instance) {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD); // Icon for active instances
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + instance.getGameName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "World: " + instance.getWorldName());
            lore.add(ChatColor.GRAY + "Players: " + instance.getTotalPlayerCount() + "/" +
            instance.getTeamSizes().values().stream()
                   .mapToInt(sizeMap -> sizeMap.getOrDefault("max", 0)) // Sum up the "max" values
                   .sum());            lore.add(ChatColor.GRAY + "State: " + instance.getState());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to join this game.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void openCreateGameMenu(Player player, String registeredGameName) {
        GameData registeredGameData = gamesManager.getGameData(registeredGameName);
    
        // Check if there are no games of the specified type
        if (registeredGameData == null) {
            player.sendMessage(ChatColor.RED + "No game with name " + registeredGameName + " is registered.");
            return; // Exit the method to avoid the exception
        }
    
        Inventory createMenu = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Create " + registeredGameName);
    
        // World selection options
        int slot = 10;
        for (String world : registeredGameData.getWorldOptions()) {
            ItemStack worldOption = createMenuItem(world, ChatColor.GRAY + "Click to select this world.");
            createMenu.setItem(slot++, worldOption);
        }
    
        // Toggle open/closed
        ItemStack toggleOpenClosed = createMenuItem(
            isInstancePublic.getOrDefault(player, false) ? "Open to Public" : "Closed to Private",
            ChatColor.YELLOW + "Click to toggle instance visibility."
        );
        createMenu.setItem(45, toggleOpenClosed); // Bottom-left corner
    
        // Create instance button
        ItemStack createInstance = createMenuItem(
            ChatColor.GREEN + "Create Instance",
            ChatColor.GRAY + "Click to create the instance."
        );
        createMenu.setItem(49, createInstance); // Center slot
    
        player.openInventory(createMenu);
    }
}

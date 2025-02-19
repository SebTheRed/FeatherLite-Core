package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import com.featherlite.pluginBin.lobbies.GamesManager.GameData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class GamesUI implements Listener {

    private final GamesManager gamesManager;
    private final InstanceManager instanceManager;
    private final Map<Player, String> chosenRegisteredGame = new HashMap<>();
    private final Map<Player, Boolean> isInstancePublic = new HashMap<>();
    private final Map<Player, String> selectedWorld = new HashMap<>();
    private final Map<Player, String> selectedInstanceId = new HashMap<>();
    private final Map<Player, Boolean> suppressCloseEvent = new HashMap<>();

    private final boolean isDebuggerOn;
    private final JavaPlugin plugin;


    public GamesUI(GamesManager gamesManager, InstanceManager instanceManager, boolean isDebuggerOn, JavaPlugin plugin) {
        this.gamesManager = gamesManager;
        this.instanceManager = instanceManager;
        this.isDebuggerOn = isDebuggerOn;
        this.plugin = plugin;
    }


    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
    
        Player player = (Player) event.getPlayer();
        if (suppressCloseEvent.getOrDefault(player, false)) return; // Skip if suppressed
    
        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());
        if (isGamesUIMenu(inventoryTitle, player)) {
            chosenRegisteredGame.remove(player);
            isInstancePublic.remove(player);
            selectedWorld.remove(player);
            selectedInstanceId.remove(player);
    
            if (isDebuggerOn) {
                plugin.getLogger().info("Cleared player-specific data for: " + player.getName());
            }
        }
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
        suppressCloseEvent.put(player, true);
        // Check if the inventory belongs to GamesUI
        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());
        if (!isGamesUIMenu(inventoryTitle, player)) {
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

                        //here gpt
                        String uuidString = ChatColor.stripColor(lore.get(lore.size() - 1)).trim();
                        selectedInstanceId.put(player, uuidString);
                        // HERE IS WHERE WE WILL STOP FOR TN
                        // TOMORROW make another menu to display
                            // game status
                                // if game is started, colored teams to join.
                                // if still lobby put in lobby.
                            // teams w/ players
                            // spectator option
                        instanceManager.addPlayerToInstance(player, instanceManager.getInstance(UUID.fromString(uuidString)));
                    }
                } else if (inventoryTitle.equalsIgnoreCase("Create " + chosenRegisteredGame.get(player))) {
                    if (isDebuggerOn) {player.sendMessage("Clicking inside game options menu");}



                    if (itemDisplayName != null && itemDisplayName.contains("Select World")) {


                        
                        GameData gameTypeData = gamesManager.getGameData(chosenRegisteredGame.get(player));
                        openWorldSelectionMenu(player, gameTypeData.getWorldOptions()); // Refresh the menu
                    
                        event.setCancelled(true);

                    
                    
                    } else if (itemDisplayName.contains("Open to Public") || itemDisplayName.equalsIgnoreCase("Closed to Public")) {
                        boolean isPublic = isInstancePublic.getOrDefault(player, true);
                        isInstancePublic.put(player, !isPublic); // Toggle state
                        if (isDebuggerOn) {player.sendMessage(ChatColor.YELLOW + "Instance is now " + (!isPublic ? "public" : "private") + ".");}
                        openCreateGameMenu(player, chosenRegisteredGame.get(player)); // Refresh the menu
                        event.setCancelled(true);

                    } else if (itemDisplayName.contains("Create Instance")) {
                        String chosenWorld = selectedWorld.get(player);
                        if (chosenWorld == null) {
                            player.sendMessage(ChatColor.YELLOW + "You must select a world before creating an instance.");
                            return;
                        }
                
                        GameInstance instance = gamesManager.startGameInstance(
                            chosenRegisteredGame.get(player), chosenWorld, isInstancePublic.get(player) ,instanceManager, player.getName(), true
                        );
                        try {
                            if (instance == null) {
                                player.sendMessage(ChatColor.RED + "No game instance found with that ID, report this to an admin!");
                                return;
                            }
                            instanceManager.addPlayerToInstance(player, instance);
                            // player.sendMessage("You have joined the game instance!");
                        } catch (IllegalArgumentException e) {
                            player.sendMessage("Invalid instance ID.");
                        }
                
                        if (instance != null) {
                            player.sendMessage(ChatColor.GREEN + "Instance created successfully: " + instance.getGameName());
                        } else {
                            player.sendMessage(ChatColor.RED + "Failed to create the instance.");
                        }
                        event.setCancelled(true);
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> suppressCloseEvent.put(player, false), 1L);
                } else if (inventoryTitle.equalsIgnoreCase("Select World Map")) {
                    event.setCancelled(true);
                    ItemStack clickedItemWorld = event.getCurrentItem();
                    if (clickedItemWorld != null && clickedItemWorld.hasItemMeta()) {
                        String worldName = ChatColor.stripColor(clickedItemWorld.getItemMeta().getDisplayName());
                        selectedWorld.put(player, worldName);
                        String gameName = chosenRegisteredGame.get(player);
                        if (isDebuggerOn) {player.sendMessage( ChatColor.GRAY + "Selected world: " + ChatColor.GREEN + worldName);}
                        openCreateGameMenu(player, gameName); // Return to the Create Game menu
                        Bukkit.getScheduler().runTaskLater(plugin, () -> suppressCloseEvent.put(player, false), 1L);
                    }
                    return;
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
        Inventory mainMenu = Bukkit.createInventory(null, 27, ChatColor.BOLD + "" + ChatColor.GREEN + "Games Menu");

        // Option 1: View/Join Games
        ItemStack joinItem = new ItemStack(org.bukkit.Material.COMPASS);
        ItemStack viewJoinGames = createMenuItem(ChatColor.LIGHT_PURPLE + "View/Join Games", ChatColor.AQUA + "Click to view or join games.", joinItem);
        mainMenu.setItem(12, viewJoinGames);

        // Option 2: Create Game
        ItemStack createItem = new ItemStack(org.bukkit.Material.ANVIL);
        ItemStack createGame = createMenuItem(ChatColor.GREEN + "Create Game", ChatColor.YELLOW + "Click to create a new game.", createItem);
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
        Inventory viewJoinMenu = Bukkit.createInventory(null, inventorySize, ChatColor.BLUE + "Create a Game");

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
        Inventory gameSelectionMenu = Bukkit.createInventory(null, inventorySize, ChatColor.BLUE + "Create a Game");

        // Populate menu with active games
        int slot = 10;
        for (GameData game : registeredGames) {
            ItemStack gameItem = createGameItem(game);
            gameSelectionMenu.setItem(slot++, gameItem);
        }

        player.openInventory(gameSelectionMenu);
    }







    
    public void openCreateGameMenu(Player player, String registeredGameName) {
        GameData registeredGameData = gamesManager.getGameData(registeredGameName);

        // Check if there are no games of the specified type
        if (registeredGameData == null) {
            player.sendMessage(ChatColor.RED + "No game with name " + registeredGameName + " is registered.");
            return; // Exit the method to avoid the exception
        }

        Inventory createMenu = Bukkit.createInventory(
            null,
            27,
            ChatColor.GREEN + "Create " + registeredGameName
        );

        // World selection option
        String worldName = selectedWorld.getOrDefault(player, "Select World");
        ItemStack worldItem = createMenuItem(
            ChatColor.AQUA + "Select World",
            worldName.equals("Select World")
                ? ChatColor.GRAY + "Click to select a world."
                : ChatColor.GRAY + "Selected world: " + ChatColor.GREEN + worldName,
            worldName.equals("Select World")
                ? new ItemStack(Material.GRASS_BLOCK)
                : new ItemStack(Material.EMERALD_BLOCK)

        );
        createMenu.setItem(11, worldItem);

        // Toggle open/closed
        ItemStack toggleOpenClosed = createMenuItem(
            isInstancePublic.getOrDefault(player, true) ? ChatColor.GREEN + "Open to Public" : ChatColor.YELLOW + "Closed to Public",
            ChatColor.GRAY + "Click to toggle instance visibility.",
            isInstancePublic.getOrDefault(player, true)
                ? new ItemStack(Material.DARK_OAK_FENCE_GATE)
                : new ItemStack(Material.DARK_OAK_FENCE)
        );
        createMenu.setItem(13, toggleOpenClosed);

        // Create instance button
        ItemStack createInstance = createMenuItem(
            ChatColor.GREEN + "Create Instance",
            ChatColor.GRAY + "Click to create the instance.",
            new ItemStack(Material.LIME_CONCRETE_POWDER)
        );
        createMenu.setItem(15, createInstance);

        player.openInventory(createMenu);
    }


    public void openWorldSelectionMenu(Player player, List<String> worlds) {
        Inventory worldSelectionMenu = Bukkit.createInventory(
            null,
            54,
            ChatColor.DARK_GREEN + "Select World Map"
        );

        int slot = 0;
        for (String world : worlds) {
            if (slot >= 54) break; // Limit to a single menu page for now
            ItemStack worldItem = new ItemStack(Material.GRASS_BLOCK);
            ItemStack menuItem = createMenuItem(
                ChatColor.YELLOW + world,
                ChatColor.GRAY + "Click to select this world.",
                worldItem
            );
            worldSelectionMenu.setItem(slot++, menuItem);
        }

        player.openInventory(worldSelectionMenu);
    }



    /**
     * Creates a menu item.
     *
     * @param name        The display name of the item.
     * @param description The description of the item.
     * @return The ItemStack representing the menu item.
     */
    private ItemStack createMenuItem(String name, String description, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
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

    private boolean isGamesUIMenu(String title, Player player) {
        return title.equals("Games Menu") ||
               title.equals("View/Join Games") ||
               title.equals("Create Game") ||
               title.equals("Create a Game") ||
               title.equals("Select World Map") ||
               title.equals("Create " + chosenRegisteredGame.get(player)) ||
               title.equals(chosenRegisteredGame.get(player));
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
        ItemStack item = new ItemStack(Material.GOLD_BLOCK); // Icon for active instances
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + instance.getGameName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.WHITE + "Created By: " + instance.getCreatedBy());
            lore.add(ChatColor.GRAY + "World: " + instance.getBaseWorldName());
            lore.add(ChatColor.GRAY + "Players: " + instance.getTotalPlayerCount());

            // instance.getTeamSizes().values().stream()
            //        .mapToInt(sizeMap -> sizeMap.getOrDefault("max", 0)) // Sum up the "max" values
            //        .sum());            lore.add(ChatColor.GRAY + "State: " + instance.getState());
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click to join this game.");
            lore.add(ChatColor.DARK_GRAY + instance.getInstanceId().toString());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

}

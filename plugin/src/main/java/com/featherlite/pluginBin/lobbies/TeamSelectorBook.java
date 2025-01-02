package com.featherlite.pluginBin.lobbies;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.NamespacedKey;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.UUID;

public class TeamSelectorBook implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey instanceKey;

    public TeamSelectorBook(JavaPlugin plugin) {
        this.plugin = plugin;
        this.instanceKey = new NamespacedKey(plugin, "game_instance_uuid");
    }

    /**
     * Give the player the team selector book.
     *
     * @param player      The player receiving the book.
     * @param instanceUuid The UUID of the GameInstance.
     */
    public void giveSelectorBook(Player player, UUID instanceUuid) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();

        // Set the display name and lore
        meta.setDisplayName("§aTeam Selector");
        meta.setLore(Collections.singletonList("§7Click to choose a team!"));

        // Store the game instance UUID in the book's metadata
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(instanceKey, PersistentDataType.STRING, instanceUuid.toString());

        book.setItemMeta(meta);

        player.getInventory().addItem(book);
    }

    
    /**
    * Handles clicks in the "Team Selection" GUI.
    *
    * @param event The InventoryClickEvent.
    */
    public void handleTeamSelectionClick(InventoryClickEvent event, InstanceManager instanceManager) {
        event.setCancelled(true); // Prevent moving items in the GUI

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.getType().toString().endsWith("_WOOL")) {
            return;
        }

        String teamName = clickedItem.getItemMeta().getDisplayName().replace("§b", "").replace(" Team", "");
        GameInstance instance = instanceManager.getInstanceForPlayer(player);

        if (instance == null) {
            player.sendMessage("§cYou are not in a game instance.");
            return;
        }

        UUID playerUuid = player.getUniqueId();
        String currentTeam = instance.getTeams().entrySet().stream()
                .filter(entry -> entry.getValue().contains(playerUuid))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        // Prevent the player from selecting the team they're already on
        if (teamName.equalsIgnoreCase(currentTeam)) {
            player.sendMessage("§eYou are already on the §b" + teamName + " §eteam.");
            return;
        }

        TeamSize teamSize = instance.getTeamSizes().get(teamName.toLowerCase());
        List<UUID> teamMembers = instance.getTeams().get(teamName);

        if (teamSize == null) {
            player.sendMessage("§cTeam does not exist. Please report this issue.");
            return;
        }

        if (teamMembers.size() >= teamSize.getMax()) {
            player.sendMessage("§cThis team is full. Please choose another team.");
            return;
        }

        // Remove player from all other teams and add them to the selected team
        instance.getTeams().values().forEach(team -> team.remove(playerUuid));
        teamMembers.add(playerUuid);

        player.sendMessage("§aYou have joined the §b" + teamName + " §ateam!");
        instance.broadcastToAllPlayers("§a" + player.getName() + " joined the §b" + teamName + " §ateam!");
        player.closeInventory();
    }


   /**
     * Open the team selection GUI for the player.
     *
     * @param player   The player opening the GUI.
     * @param instance The GameInstance associated with the player.
     */
    public void openTeamSelectionGUI(Player player, GameInstance instance) {
        try {
            Bukkit.getLogger().info("Opening Team Selection GUI for player: " + player.getName() + ", Instance ID: " + instance.getInstanceId());

            int rows = Math.max(1, (int) Math.ceil(instance.getTeams().size() / 9.0));
            Inventory gui = Bukkit.createInventory(null, rows * 9, "§6Team Selection");

            UUID playerUuid = player.getUniqueId();
            String currentTeam = instance.getTeams().entrySet().stream()
                    .filter(entry -> entry.getValue().contains(playerUuid))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null); // Determine the current team the player is on

            Bukkit.getLogger().info("Player " + player.getName() + " is currently on team: " + currentTeam);

            int slot = 0;
            for (Map.Entry<String, List<UUID>> entry : instance.getTeams().entrySet()) {
                String teamName = entry.getKey();
                List<UUID> teamMembers = entry.getValue();
                TeamSize teamSize = instance.getTeamSizes().get(teamName.toLowerCase());

                if (teamSize == null) {
                    Bukkit.getLogger().warning("Team '" + teamName + "' in instance " + instance.getInstanceId() + " has no TeamSize defined.");
                    continue;
                }

                ItemStack teamItem = getTeamItem(teamName);

                ItemMeta meta = teamItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName("§b" + teamName + " Team");
                    if (teamName.equalsIgnoreCase(currentTeam)) {
                        meta.setLore(Arrays.asList(
                                "§7Current Size: §e" + teamMembers.size() + "/" + teamSize.getMax(),
                                "§6(Your Team)"
                        ));
                    } else {
                        meta.setLore(Arrays.asList(
                                "§7Current Size: §e" + teamMembers.size() + "/" + teamSize.getMax(),
                                teamMembers.size() >= teamSize.getMax() ? "§cTeam Full" : "§aClick to join!"
                        ));
                    }
                    teamItem.setItemMeta(meta);
                } else {
                    Bukkit.getLogger().warning("Failed to set meta for team item: " + teamName);
                }

                gui.setItem(slot++, teamItem);
            }

            player.openInventory(gui);
            Bukkit.getLogger().info("Team Selection GUI successfully opened for player: " + player.getName());

        } catch (Exception e) {
            Bukkit.getLogger().severe("An error occurred while opening Team Selection GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
            player.sendMessage("§cAn error occurred while opening the team selection menu. Please report this to the admin.");
        }
    }



    /**
     * Gets a wool ItemStack matching the team's color name.
     *
     * @param teamName The name of the team.
     * @return An ItemStack of colored wool or white wool if no match is found.
     */
    private ItemStack getTeamItem(String teamName) {
        Material woolMaterial;

        switch (teamName.toLowerCase()) {
            case "white":
                woolMaterial = Material.WHITE_WOOL;
                break;
            case "light_gray":
                woolMaterial = Material.LIGHT_GRAY_WOOL;
                break;
            case "gray":
                woolMaterial = Material.GRAY_WOOL;
                break;
            case "black":
                woolMaterial = Material.BLACK_WOOL;
                break;
            case "red":
                woolMaterial = Material.RED_WOOL;
                break;
            case "orange":
                woolMaterial = Material.ORANGE_WOOL;
                break;
            case "yellow":
                woolMaterial = Material.YELLOW_WOOL;
                break;
            case "lime":
                woolMaterial = Material.LIME_WOOL;
                break;
            case "green":
                woolMaterial = Material.GREEN_WOOL;
                break;
            case "light_blue":
                woolMaterial = Material.LIGHT_BLUE_WOOL;
                break;
            case "cyan":
                woolMaterial = Material.CYAN_WOOL;
                break;
            case "blue":
                woolMaterial = Material.BLUE_WOOL;
                break;
            case "purple":
                woolMaterial = Material.PURPLE_WOOL;
                break;
            case "magenta":
                woolMaterial = Material.MAGENTA_WOOL;
                break;
            case "pink":
                woolMaterial = Material.PINK_WOOL;
                break;
            case "brown":
                woolMaterial = Material.BROWN_WOOL;
                break;
            default:
                woolMaterial = Material.WHITE_WOOL; // Default to white wool if no match
                Bukkit.getLogger().warning("Unknown team color: " + teamName + ". Defaulting to white wool.");
                break;
        }

        return new ItemStack(woolMaterial);
    }

}

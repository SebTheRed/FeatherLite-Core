package com.featherlite.pluginBin.displays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class DisplayPieceManager {
    private final JavaPlugin plugin;
    private final Map<String, DisplayPiece> displayPieces = new HashMap<>();



    public DisplayPieceManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTextDisplayCleanupClock();

    }

    /**
     * Create a new TextDisplay.
     * @param id The unique id of the display.
     * @param world The world the display is placed in.
     * @param location The location the display spawns at.
     * @param text The text this display will show.
     * @param isPersistent Whether or not the display persists when a chunk is un-loaded.
     */
    public DisplayPiece createTextDisplay(String id, World world, Location location, String text, boolean isPersistent) {
        TextDisplay textDisplay = world.spawn(location, TextDisplay.class);
        textDisplay.setText(text);
        textDisplay.setPersistent(isPersistent);
        DisplayPiece displayPiece = new DisplayPiece(textDisplay, plugin);
        displayPieces.put(id, displayPiece);
        return displayPiece;
    }

    /**
     * Create a new BlockDisplay.
     * @param id The unique id of the display.
     * @param world The world the display is placed in.
     * @param location The location the display spawns at.
     * @param material The block this display will appear as.
     * @param isPersistent Whether or not the display persists when a chunk is un-loaded.
     */
    public DisplayPiece createBlockDisplay(String id, World world, Location location, Material material, boolean isPersistent) {
        Display display = world.spawn(location, org.bukkit.entity.BlockDisplay.class);
        display.setPersistent(isPersistent);
        DisplayPiece displayPiece = new DisplayPiece(display, plugin);
        displayPiece.setBlock(material);
        displayPieces.put(id, displayPiece);
        return displayPiece;
    }

    /**
     * Create a new ItemDisplay.
     * @param id The unique id of the display.
     * @param world The world the display is placed in.
     * @param location The location the display spawns at.
     * @param itemStack The item this display will appear as.
     * @param isPersistent Whether or not the display persists when a chunk is un-loaded.
     */
    public DisplayPiece createItemDisplay(String id, World world, Location location, ItemStack itemStack, boolean isPersistent) {
        ItemDisplay itemDisplay = world.spawn(location, ItemDisplay.class);
        itemDisplay.setItemStack(itemStack);
        itemDisplay.setPersistent(isPersistent);
        DisplayPiece displayPiece = new DisplayPiece(itemDisplay, plugin);
        displayPieces.put(id, displayPiece);
        return displayPiece;
    }


    public void startTextDisplayCleanupClock() {
    // Run the task every 5 seconds (100 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.getWorlds().stream()
                .filter(world -> !world.getPlayers().isEmpty()) // Only process worlds with active players
                .forEach(world -> {
                    world.getEntitiesByClass(TextDisplay.class).forEach(display -> {
                        // Check if the display text contains "HP" or "XP"
                        String text = display.getText();
                        if (text != null && (text.contains("♡") || text.contains("XP"))) {
                            display.remove(); // Remove the display
                            plugin.getLogger().info("Removed TextDisplay: " + text); // Log for debugging
                        }
                    });
                });
        }, 0L, 100L); // Initial delay = 0, repeat every 5 seconds (100 ticks)
    }
    
    /**
     * Remove a display piece.
     * @param id The id of the display to be removed.
     */
    public boolean removeDisplay(String id) {
        DisplayPiece piece = displayPieces.remove(id);
        if (piece != null) {
            // Forcefully remove the entity from the world
            if (piece.getDisplayEntity().isValid()) {
                piece.remove();
            }
            return true;
        }
        return false; // Indicate failure to remove
    }
    

    /**
     * Clear all displays.
     */
    public void clearAllDisplays() {
        displayPieces.values().forEach(DisplayPiece::remove);
        displayPieces.clear();
    }



    /**
     * Create a TextDisplay and mount it to a player.
     */
    public void mountDisplayOnPlayer(Player player, String text) {
        // Spawn the TextDisplay at the player's location
        Location location = player.getLocation();
        String id = "display_" + player.getUniqueId(); // Generate a unique ID for tracking

        DisplayPiece displayPiece = createTextDisplay(
                id,
                player.getWorld(),
                location,
                text,
                false // Non-persistent
        );

        // Customize the TextDisplay
        displayPiece.setBillboard(org.bukkit.entity.Display.Billboard.CENTER); // Center the text
        displayPiece.setBrightness(15, 15); // Max brightness
        displayPiece.setShadowStrength(0.5f); // Subtle shadow

        // Mount the TextDisplay to the player
        player.addPassenger(displayPiece.getDisplayEntity());
    }



}

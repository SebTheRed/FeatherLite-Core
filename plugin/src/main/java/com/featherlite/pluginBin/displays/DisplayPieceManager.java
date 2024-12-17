package com.featherlite.pluginBin.displays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
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

    /**
     * Remove a display piece.
     * @param id The id of the display to be removed.
     */
    public boolean removeDisplay(String id) {
        DisplayPiece piece = displayPieces.remove(id);
        if (piece != null) {
            piece.remove();
            return true;
        }
        return false;
    }

    /**
     * Clear all displays.
     */
    public void clearAllDisplays() {
        displayPieces.values().forEach(DisplayPiece::remove);
        displayPieces.clear();
    }
}

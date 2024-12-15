
// ====== Example Usage ====== //

// // Instantialize Class
// DisplayManager displayManager = new DisplayManager();
// World world = Bukkit.getWorld("world");
// Location location = new Location(world, 100, 65, 100);

// // Options map for customizations
// Map<String, Object> options = new HashMap<>();
// options.put("billboard", Display.Billboard.FIXED);
// options.put("brightness", new Display.Brightness(15, 0)); // Max light level
// options.put("view_range", 50.0f);
// options.put("shadow_radius", 2.0f);
// options.put("shadow_strength", 0.5f);
// options.put("display_width", 3.0f);
// options.put("display_height", 1.0f);
// options.put("glow_color_override", Color.AQUA);

// // Create a Text Display
// Display textDisplay = displayManager.createDisplay(world, location, "text", "Welcome to Spawn!", options);
// displayManager.registerDisplay("welcome_message", textDisplay);


package com.featherlite.pluginBin.utils;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DisplayManager {

    // Store active displays with unique IDs
    private final Map<String, Display> activeDisplays = new HashMap<>();

    /**
     * Create a display entity with comprehensive customization options.
     *
     * @param world       The world where the display will be spawned.
     * @param location    The location to spawn the display.
     * @param displayType The type of display (e.g., "text", "block", "item").
     * @param content     The content for the display (varies by type).
     * @param options     A map of additional customization options.
     * @return The created Display entity.
     */
    public Display createDisplay(World world, Location location, String displayType, Object content, Map<String, Object> options) {
        Display display;

        // Determine the type of display
        switch (displayType.toLowerCase()) {
            case "text":
                display = createTextDisplay(world, location, content);
                break;
            case "block":
                display = createBlockDisplay(world, location, content);
                break;
            case "item":
                display = createItemDisplay(world, location, content);
                break;
            default:
                throw new IllegalArgumentException("Invalid display type: " + displayType);
        }

        // Apply customizations
        customizeDisplay(display, options);

        return display;
    }

    /**
     * Create a Text Display.
     */
    private TextDisplay createTextDisplay(World world, Location location, Object content) {
        if (!(content instanceof String)) {
            throw new IllegalArgumentException("TextDisplay requires content to be a String.");
        }

        TextDisplay textDisplay = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setText((String) content);
        return textDisplay;
    }

    /**
     * Create a Block Display.
     */
    private BlockDisplay createBlockDisplay(World world, Location location, Object content) {
        if (!(content instanceof Material)) {
            throw new IllegalArgumentException("BlockDisplay requires content to be a Material.");
        }

        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(location, EntityType.BLOCK_DISPLAY);
        blockDisplay.setBlock(Bukkit.createBlockData((Material) content));
        return blockDisplay;
    }

    /**
     * Create an Item Display.
     */
    private ItemDisplay createItemDisplay(World world, Location location, Object content) {
        if (!(content instanceof ItemStack)) {
            throw new IllegalArgumentException("ItemDisplay requires content to be an ItemStack.");
        }

        ItemDisplay itemDisplay = (ItemDisplay) world.spawnEntity(location, EntityType.ITEM_DISPLAY);
        itemDisplay.setItemStack((ItemStack) content);
        return itemDisplay;
    }

    /**
     * Apply customizations to a display using options.
     */
    private void customizeDisplay(Display display, Map<String, Object> options) {
        if (options.containsKey("billboard")) {
            display.setBillboard((Display.Billboard) options.get("billboard"));
        }
        if (options.containsKey("brightness")) {
            display.setBrightness((Display.Brightness) options.get("brightness"));
        }
        if (options.containsKey("view_range")) {
            display.setViewRange((Float) options.get("view_range"));
        }
        if (options.containsKey("shadow_radius")) {
            display.setShadowRadius((Float) options.get("shadow_radius"));
        }
        if (options.containsKey("shadow_strength")) {
            display.setShadowStrength((Float) options.get("shadow_strength"));
        }
        if (options.containsKey("interpolation_delay")) {
            display.setInterpolationDelay((Integer) options.get("interpolation_delay"));
        }
        if (options.containsKey("interpolation_duration")) {
            display.setInterpolationDuration((Integer) options.get("interpolation_duration"));
        }
        if (options.containsKey("teleport_duration")) {
            display.setTeleportDuration((Integer) options.get("teleport_duration"));
        }
        if (options.containsKey("transformation")) {
            display.setTransformation((Transformation) options.get("transformation"));
        }
        if (options.containsKey("display_width")) {
            display.setDisplayWidth((Float) options.get("display_width"));
        }
        if (options.containsKey("display_height")) {
            display.setDisplayHeight((Float) options.get("display_height"));
        }
        if (options.containsKey("glow_color_override")) {
            display.setGlowColorOverride((Color) options.get("glow_color_override"));
        }
    }

    /**
     * Remove a display by its unique identifier.
     */
    public boolean removeDisplay(String id) {
        Display display = activeDisplays.remove(id);
        if (display != null) {
            display.remove();
            return true;
        }
        return false;
    }

    /**
     * Get an active display by its unique identifier.
     */
    public Display getDisplay(String id) {
        return activeDisplays.get(id);
    }

    /**
     * Register a display with a unique identifier.
     */
    public void registerDisplay(String id, Display display) {
        activeDisplays.put(id, display);
    }

    /**
     * Clear all active displays.
     */
    public void clearAllDisplays() {
        for (Display display : activeDisplays.values()) {
            display.remove();
        }
        activeDisplays.clear();
    }
}

package com.featherlite.pluginBin.displays;
import com.featherlite.pluginBin.utils.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;


public class PlayerDisplayName {
    private final Player player;
    private final DisplayPieceManager displayPieceManager;
    private DisplayPiece displayPiece;
    private final JavaPlugin plugin;

    public PlayerDisplayName(Player player, DisplayPieceManager displayPieceManager, JavaPlugin plugin) {
        this.player = player;
        this.displayPieceManager = displayPieceManager;
        this.plugin = plugin;

        createDisplay();
    }

    /**
     * Create the TextDisplay for the player.
     */
    private void createDisplay() {
        Location location = player.getLocation().add(0, 3, 0); // Start above the player's head
        String id = "display_" + player.getUniqueId();

        // Create the TextDisplay using DisplayPieceManager
        displayPiece = displayPieceManager.createTextDisplay(id, player.getWorld(), location, "Default Name", false);

        // Customize the TextDisplay
        displayPiece.setBillboard(org.bukkit.entity.Display.Billboard.CENTER); // Face the player
        displayPiece.setBrightness(15, 15); // Max brightness
        displayPiece.setShadowStrength(0.5f); // Add shadow for visibilitya

        // Apply default color-coded text NOT WORKING >l(
        updateText("&aDefault Name"); // Green text as an example
    }

    /**
     * Update the text of the display.
     */
    public void updateText(String newText) {
        if (displayPiece != null) {
            String parsedText = ChatColor.translateAlternateColorCodes('&', newText);
            displayPiece.setText(parsedText);

        }
    }

    /**
     * Start a task to constantly teleport the display to the player's location.
     */
    // private void startTrackingTask() {
    //     Bukkit.getScheduler().runTaskTimer(plugin, () -> {
    //         if (!player.isOnline() || !displayPiece.getDisplayEntity().isValid()) {
    //             removeDisplay();
    //             return;
    //         }

    //         // Update the display position to stay above the player's head
    //         Location newLocation = player.getLocation().add(0, 2.3, 0);
    //         displayPiece.getDisplayEntity().teleport(newLocation);
    //     }, 0L, 2L); // Update every 2 ticks (10 times per second)
    // }

    /**
     * Remove the TextDisplay when no longer needed.
     */
    public void removeDisplay() {
        if (displayPiece != null) {
            displayPieceManager.removeDisplay("display_" + player.getUniqueId());
            displayPiece = null;
        }
    }

        /**
     * Apply a translation transformation to the TextDisplay.
     */
    private void applyTranslation(DisplayPiece displayPiece, float offsetX, float offsetY, float offsetZ) {
        // Create a Transformation object with a translation offset
        Transformation transformation = new Transformation(
            new Vector3f(offsetX, offsetY, offsetZ), // Translation offset (position adjustment)
            new AxisAngle4f(0, 0, 0, 1), // No left rotation (identity)
            new Vector3f(1, 1, 1), // No scaling (identity)
            new AxisAngle4f(0, 0, 0, 1)  // No right rotation (identity)
        );

        // Apply the transformation to the display
        displayPiece.getDisplayEntity().setTransformation(transformation);
    }

}

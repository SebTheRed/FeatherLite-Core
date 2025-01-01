package com.featherlite.pluginBin.displays;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.bukkit.plugin.java.JavaPlugin;

public class DisplayPiece {
    private final Display displayEntity; // The underlying display entity
    private final JavaPlugin plugin;
    private Vector3f currentTranslation = new Vector3f(0, 0, 0);
    private Vector3f currentScale = new Vector3f(1, 1, 1);
    private AxisAngle4f currentLeftRotation = new AxisAngle4f();
    private AxisAngle4f currentRightRotation = new AxisAngle4f();

    /**
     * Constructor for creating a new DisplayPiece.
     *
     * @param displayEntity The Bukkit Display entity (TextDisplay, ItemDisplay, BlockDisplay).
     * @param plugin
     */
    public DisplayPiece(Display displayEntity, JavaPlugin plugin) {
        this.displayEntity = displayEntity;
        this.plugin = plugin;

        applyTransformation();
    }

    /**
     * Update the display's text (for TextDisplay).
     */
    public void setText(String text) {
        if (displayEntity instanceof TextDisplay textDisplay) {
            textDisplay.setText(text);
        }
    }

    /**
     * Gets the exact entity - for removal sake.
     * @return
     */
    public Display getDisplayEntity() {
        return this.displayEntity;
    }

    /**
     * Set the display billboard mode.
     */
    public void setBillboard(Display.Billboard billboard) {
        displayEntity.setBillboard(billboard);
    }

    /**
     * Set the display brightness (light and sky).
     */
    public void setBrightness(int blockLight, int skyLight) {
        displayEntity.setBrightness(new Display.Brightness(blockLight, skyLight));
    }

    /**
     * Set the display shadow strength.
     */
    public void setShadowStrength(float strength) {
        displayEntity.setShadowStrength(strength);
    }

    /**
     * Set the block material (for BlockDisplay).
     */
    public void setBlock(Material material) {
        if (displayEntity instanceof BlockDisplay blockDisplay) {
            blockDisplay.setBlock(material.createBlockData());
        }
    }

    /**
     * Set the item (for ItemDisplay).
     */
    public void setItem(ItemStack itemStack) {
        if (displayEntity instanceof ItemDisplay itemDisplay) {
            itemDisplay.setItemStack(itemStack);
        }
    }

    /**
     * Set the glow color.
     */
    public void setGlowColor(Color color) {
        displayEntity.setGlowColorOverride(color);
    }

    // /**
    //  * Move the display smoothly to a target location.
    //  *
    //  * @param targetLocation The target location.
    //  * @param durationTicks  The duration of the movement in ticks.
    //  */
    // public void move(Location targetLocation, int durationTicks) {
    //     // Calculate the relative offset from the display's current position
    //     Vector3f startTranslation = currentTranslation;
        
    //     Location currentLocation = displayEntity.getLocation();

    //     // Calculate the delta translation with directional correction
    //     Vector3f deltaTranslation = new Vector3f(
    //         targetLocation.getX() >= currentLocation.getX()
    //             ? (float) (targetLocation.getX() - currentLocation.getX())
    //             : (float) (currentLocation.getX() - targetLocation.getX()),
    
    //         targetLocation.getY() >= currentLocation.getY()
    //             ? (float) (targetLocation.getY() - currentLocation.getY())
    //             : (float) (currentLocation.getY() - targetLocation.getY()),
    
    //         targetLocation.getZ() >= currentLocation.getZ()
    //             ? (float) (targetLocation.getZ() - currentLocation.getZ())
    //             : (float) (currentLocation.getZ() - targetLocation.getZ())
    //     );
    
    //     // Create matrices for the animation
    //     Matrix4f startMatrix = new Matrix4f().translate(startTranslation);
    //     Matrix4f endMatrix = new Matrix4f().translate(startTranslation.add(deltaTranslation, new Vector3f()));
    
    //     // Schedule the animation
    //     scheduleMatrixAnimation(startMatrix, endMatrix, durationTicks);
    // }

    public void move(Location targeLocation) {
        displayEntity.teleport(targeLocation);
        // displayEntity.setTeleportDuration(40);
        // displayEntity.setTeleportDuration(durationTicks);


    }

    /**
     * Scale the display smoothly to a target scale.
     *
     * @param targetScale   The target scale (uniform scaling).
     * @param durationTicks The duration of the scaling in ticks.
     */
    public void scale(float targetScale) {
        displayEntity.setTransformation(
            new Transformation(
                    new Vector3f(), // no translation
                    new AxisAngle4f(), // no left rotation
                    new Vector3f(targetScale, targetScale, targetScale), // scale up by a factor of 2 on all axes
                    new AxisAngle4f() // no right rotation
            )
        );
    }

    /**
     * Rotate the display smoothly on the Y-axis.
     *
     * @param angleDegrees  The target rotation angle in degrees.
     * @param durationTicks The duration of the rotation in ticks.
     */
    public void rotate(Vector3f axis, float angleDegrees, int durationTicks) {
        Matrix4f startMatrix = new Matrix4f().rotate(currentLeftRotation);
        Matrix4f endMatrix = new Matrix4f().rotate((float) Math.toRadians(angleDegrees), axis);

        scheduleMatrixAnimation(startMatrix, endMatrix, durationTicks);
    }

    /**
     * Remove the display entity.
     */
    public void remove() {
        displayEntity.remove();
    }

    // ======= Helper Methods ======= //

    /**
     * Apply the current transformation to the display entity.
     */
    private void applyTransformation() {
        Transformation transformation = new Transformation(
                currentTranslation,
                currentLeftRotation,
                currentScale,
                currentRightRotation
        );
        displayEntity.setTransformation(transformation);
        displayEntity.setInterpolationDelay(0);
        displayEntity.setInterpolationDuration(20); // Smooth animation duration
    }

    
    private void scheduleMatrixAnimation(Matrix4f startMatrix, Matrix4f endMatrix, int durationTicks) {
        final BukkitTask[] taskHolder = new BukkitTask[1]; // Wrapper to hold the task reference
        
        // Set interpolation properties once before the task starts
        displayEntity.setInterpolationDelay(0);
        displayEntity.setInterpolationDuration(durationTicks);
        
        taskHolder[0] = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int elapsed = 0;
    
            @Override
            public void run() {
                if (!displayEntity.isValid()) { // Ensure display is still valid
                    cancelTask(taskHolder[0]);
                    return;
                }
    
                if (elapsed >= durationTicks) {
                    displayEntity.setTransformationMatrix(endMatrix);
                    cancelTask(taskHolder[0]);
                    return;
                }
    
                // Interpolate between the start and end matrices
                float progress = (float) elapsed / durationTicks;
                Matrix4f currentMatrix = new Matrix4f(startMatrix).lerp(endMatrix, progress);
                displayEntity.setTransformationMatrix(currentMatrix);
    
                elapsed++;
            }
        }, 1L, 1L);
    }
    
    
    /**
     * Helper method to cancel a running task.
     *
     * @param task The BukkitTask to cancel.
     */
    private void cancelTask(BukkitTask task) {
        if (task != null) {
            task.cancel();
        }
    }

}

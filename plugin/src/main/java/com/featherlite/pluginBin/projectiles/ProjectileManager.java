package com.featherlite.pluginBin.projectiles;

import com.featherlite.pluginBin.displays.DisplayPiece;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProjectileManager {
    private final JavaPlugin plugin;
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private BukkitTask task;

    public ProjectileManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTickLoop();
    }

    /**
     * Launch a new projectile.
     *
     * @param start         The starting location of the projectile.
     * @param direction     The direction the projectile will travel.
     * @param speed         The speed of the projectile in blocks per tick.
     * @param lifetime      The lifetime of the projectile in ticks.
     * @param displayPiece  The visual representation of the projectile (can be null).
     * @param onCollision   The logic to run when the projectile collides with something.
     * @param onUpdate      The logic to run every tick while the projectile is active.
     */
    public void launchProjectile(
            Location start,
            Vector direction,
            double speed,
            int lifetime,
            DisplayPiece displayPiece,
            Consumer<ProjectileHitResult> onCollision,
            Consumer<Projectile> onUpdate
    ) {
        Projectile projectile = new Projectile(
                start,
                direction,
                speed,
                lifetime,
                displayPiece,
                onCollision,
                onUpdate
        );
        activeProjectiles.add(projectile);
    }

    /**
     * Starts the tick loop to update all projectiles.
     */
    private void startTickLoop() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            activeProjectiles.removeIf(projectile -> !projectile.update());
        }, 0L, 1L);
    }

    /**
     * Stops the manager and clears all active projectiles.
     */
    public void shutdown() {
        if (task != null) task.cancel();
        activeProjectiles.forEach(Projectile::destroy);
        activeProjectiles.clear();
    }

    /**
     * Clears all active projectiles.
     */
    public void clearProjectiles() {
        activeProjectiles.forEach(Projectile::destroy);
        activeProjectiles.clear();
    }

    /**
     * Retrieves a list of all active projectiles.
     *
     * @return A list containing all active projectiles.
     */
    public List<Projectile> getActiveProjectiles() {
        return new ArrayList<>(activeProjectiles);
    }
}

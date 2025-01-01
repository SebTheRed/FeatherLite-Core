package com.featherlite.pluginBin.projectiles;

import com.featherlite.pluginBin.displays.DisplayPiece;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.FluidCollisionMode;
import org.bukkit.util.Vector;

import java.util.function.Consumer;

public class Projectile {
    private Player caster;
    private Location currentLocation;                     // Current position of the projectile
    private final Vector direction;                       // Direction vector for movement
    private final double speed;                           // Speed of the projectile
    private int lifetime;                                 // Ticks until the projectile despawns
    private final World world;                            // World the projectile exists in
    private final Consumer<ProjectileHitResult> onCollision; // Callback for hits
    private final Consumer<Projectile> onUpdate;          // Callback for updates

    private final DisplayPiece displayPiece;              // Visual representation of the projectile
    private boolean isAlive;                              // Tracks whether the projectile is active
    private final Location finalLocation;                 // Precomputed final position of the projectile

    

    public Projectile(Player caster, Location start, Vector direction, double speed, int lifetime,
                      DisplayPiece displayPiece,
                      Consumer<ProjectileHitResult> onCollision, Consumer<Projectile> onUpdate) {
        this.caster = caster;
        this.currentLocation = start.clone();
        this.direction = direction.normalize();
        this.speed = speed;
        this.lifetime = lifetime;
        this.world = start.getWorld();
        this.displayPiece = displayPiece;
        this.onCollision = onCollision;
        this.onUpdate = onUpdate;
        this.isAlive = true;

        // Precompute the farthest possible location for the projectile
        this.finalLocation = start.clone().add(direction.clone().multiply(speed * lifetime));

    }

    /**
     * Updates the projectile's position and checks for collisions.
     *
     * @return true if the projectile is still active, false otherwise.
     */
    public boolean update() {
        if (!isAlive || lifetime-- <= 0) {
            destroy();
            return false; // Lifetime expired
        }

        // Calculate next position
        Location nextLocation = currentLocation.clone().add(direction.clone().multiply(speed));

        // Check for collision using raycasting
        ProjectileHitResult hitResult = checkCollision(currentLocation, nextLocation);

        if (hitResult != null) {
            onCollision.accept(hitResult); // Trigger collision callback
            destroy();
            return false;
        }

        // Update the projectile's position
        currentLocation = nextLocation;
        if (displayPiece != null) {
            displayPiece.move(nextLocation);
        }

        // Trigger the onUpdate callback (user handles particles or other effects)
        if (onUpdate != null) {
            onUpdate.accept(this);
        }

        return true; // Still active
    }

    /**
     * Checks for collisions using raycasting.
     */
    private ProjectileHitResult checkCollision(Location start, Location end) {
        var result = world.rayTrace(start, direction, speed,
                FluidCollisionMode.NEVER, true, 0.5,
                entity -> !(entity instanceof Display && entity != caster));

        if (result != null) {
            if (result.getHitEntity() != null) {
                return new ProjectileHitResult(result.getHitEntity(), result.getHitPosition());
            }
            if (result.getHitBlock() != null) {
                return new ProjectileHitResult(result.getHitBlock(), result.getHitPosition());
            }
        }

        return null; // No collision
    }

    /**
     * Destroys the projectile and its visual representation.
     */
    public void destroy() {
        isAlive = false;
        if (displayPiece != null) {
            displayPiece.remove();
        }
    }

    public boolean isAlive() {
        return isAlive;
    }

    public Location getLocation() {
        return currentLocation.clone();
    }
}

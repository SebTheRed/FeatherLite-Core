package com.featherlite.pluginBin.projectiles;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ProjectileHitResult {
    private final Entity hitEntity; // The entity that was hit, if any
    private final Block hitBlock;   // The block that was hit, if any
    private final Vector hitPosition; // The exact position of the collision

    /**
     * Constructor for when the projectile hits an entity.
     *
     * @param hitEntity   The entity that was hit.
     * @param hitPosition The position where the collision occurred.
     */
    public ProjectileHitResult(Entity hitEntity, Vector hitPosition) {
        this.hitEntity = hitEntity;
        this.hitBlock = null;
        this.hitPosition = hitPosition;
    }

    /**
     * Constructor for when the projectile hits a block.
     *
     * @param hitBlock    The block that was hit.
     * @param hitPosition The position where the collision occurred.
     */
    public ProjectileHitResult(Block hitBlock, Vector hitPosition) {
        this.hitEntity = null;
        this.hitBlock = hitBlock;
        this.hitPosition = hitPosition;
    }

    /**
     * Checks if the collision was with an entity.
     *
     * @return true if an entity was hit.
     */
    public boolean hasHitEntity() {
        return hitEntity != null;
    }

    /**
     * Checks if the collision was with a block.
     *
     * @return true if a block was hit.
     */
    public boolean hasHitBlock() {
        return hitBlock != null;
    }

    /**
     * Gets the entity that was hit, if any.
     *
     * @return The hit entity, or null if no entity was hit.
     */
    public Entity getHitEntity() {
        return hitEntity;
    }

    /**
     * Gets the block that was hit, if any.
     *
     * @return The hit block, or null if no block was hit.
     */
    public Block getHitBlock() {
        return hitBlock;
    }

    /**
     * Gets the position of the collision.
     *
     * @return The collision position.
     */
    public Vector getHitPosition() {
        return hitPosition;
    }
}

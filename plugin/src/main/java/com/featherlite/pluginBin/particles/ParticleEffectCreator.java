
// ====== Example Usage ====== //


package com.featherlite.pluginBin.particles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ParticleEffectCreator {
private final ParticleManager particleManager;

    public ParticleEffectCreator(ParticleManager particleManager) {
        this.particleManager = particleManager;
    }
    // A class to encapsulate particle effect configurations
    public static class ParticleEffect {
        private final Particle particleType;
        private final int count;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private final double speed;
        private final Object data;

        public ParticleEffect(Particle particleType, int count, double offsetX, double offsetY, double offsetZ, double speed, Object data) {
            this.particleType = particleType;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.speed = speed;
            this.data = data;
        }

        public Particle getParticleType() {
            return particleType;
        }

        public int getCount() {
            return count;
        }

        public double getOffsetX() {
            return offsetX;
        }

        public double getOffsetY() {
            return offsetY;
        }

        public double getOffsetZ() {
            return offsetZ;
        }

        public double getSpeed() {
            return speed;
        }

        public Object getData() {
            return data;
        }
    }

    // Store all active particle effects
    private final List<ParticleEffect> activeEffects = new ArrayList<>();

    /**
     * Add a particle effect to the manager.
     */
    public void addParticleEffect(ParticleEffect effect) {
        activeEffects.add(effect);
    }


    /**
     * Play all registered effects at a specific location.
     */
    public void playAllEffects(World world, Location location) {
        for (ParticleEffect effect : activeEffects) {
            playParticleEffect(world, location, effect);
        }
    }

    /**
     * Play all registered effects for a specific player.
     */
    public void playAllEffectsForPlayer(Player player) {
        Location location = player.getLocation();
        for (ParticleEffect effect : activeEffects) {
            player.spawnParticle(
                    effect.getParticleType(),
                    location,
                    effect.getCount(),
                    effect.getOffsetX(),
                    effect.getOffsetY(),
                    effect.getOffsetZ(),
                    effect.getSpeed(),
                    effect.getData()
            );
        }
    }

    /**
     * Clear all active effects from the manager.
     */
    public void clearEffects() {
        activeEffects.clear();
    }

    /**
     * Create a particle effect based on the particle type.
     * Handles specialized data automatically based on particle requirements.
     */
    public static ParticleEffect createEffect(
            Particle particleType,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            double speed,
            Object data // Use null if no data is needed
    ) {
        // Handle specialized cases based on the particle type
        switch (particleType) {
            case DUST -> {
                if (data instanceof Color color) {
                    return new ParticleEffect(
                            particleType,
                            count,
                            offsetX,
                            offsetY,
                            offsetZ,
                            speed,
                            new Particle.DustOptions(color, 1.0F) // Default size is 1.0F
                    );
                } else if (data instanceof Particle.DustOptions dustOptions) {
                    return new ParticleEffect(
                            particleType,
                            count,
                            offsetX,
                            offsetY,
                            offsetZ,
                            speed,
                            dustOptions // Pass DustOptions directly if provided
                    );
                }
                throw new IllegalArgumentException("REDSTONE particles require a Color object or DustOptions as data.");
            }
            

            case DUST_COLOR_TRANSITION -> {
                if (data instanceof Particle.DustTransition dustTransition) {
                    return new ParticleEffect(particleType, count, offsetX, offsetY, offsetZ, speed, dustTransition);
                }
                throw new IllegalArgumentException("DUST_COLOR_TRANSITION particles require a DustTransition object as data.");
            }

            case DUST_PILLAR, FALLING_DUST -> {
                if (data instanceof Material material) {
                    return new ParticleEffect(
                            particleType,
                            count,
                            offsetX,
                            offsetY,
                            offsetZ,
                            speed,
                            material.createBlockData()
                    );
                }
                throw new IllegalArgumentException("BLOCK_CRACK, BLOCK_DUST, and FALLING_DUST particles require a Material object as data.");
            }

            case ITEM -> {
                if (data instanceof ItemStack itemStack) {
                    return new ParticleEffect(
                            particleType,
                            count,
                            offsetX,
                            offsetY,
                            offsetZ,
                            speed,
                            itemStack
                    );
                }
                throw new IllegalArgumentException("ITEM_CRACK particles require an ItemStack object as data.");
            }

            case SCULK_CHARGE -> {
                if (data instanceof Float chargeIntensity) {
                    return new ParticleEffect(particleType, count, offsetX, offsetY, offsetZ, speed, chargeIntensity);
                }
                throw new IllegalArgumentException("SCULK_CHARGE particles require a Float value as data.");
            }

            case SHRIEK -> {
                if (data instanceof Integer delay) {
                    return new ParticleEffect(particleType, count, offsetX, offsetY, offsetZ, speed, delay);
                }
                throw new IllegalArgumentException("SHRIEK particles require an Integer delay as data.");
            }

            // case VIBRATION -> {
            //     if (data instanceof Particle.VIBRATION vibration) {
            //         return new ParticleEffect(particleType, count, offsetX, offsetY, offsetZ, speed, vibration);
            //     }
            //     throw new IllegalArgumentException("VIBRATION particles require a Vibration object as data.");
            // }

            default -> {
                // Generic particles without specialized data
                return new ParticleEffect(particleType, count, offsetX, offsetY, offsetZ, speed, null);
            }
        }
    }



    // ====== SHAPE DRAWING METHODS ====== //

    /**
     * Creates a static particle ring and registers it with the ParticleManager.
     *
     * @param location The center of the ring.
     * @param radius   The radius of the ring.
     * @param effect   The particle effect to use.
     * @param points   The number of particles in the ring (higher = smoother circle).
     * @param duration The duration of the effect in ticks.
     * @param rotationAxis The axis of rotation ("X", "Y", "Z") or null for no rotation.
    */
    public void createParticleRing(Location location, double radius, ParticleEffect effect, int points, int duration, String rotationAxis) {
        World world = location.getWorld();
        if (world == null) return;

        // Register the effect
        particleManager.registerEffect(new ParticleManager.ActiveParticleEffect(
                location, effect, duration,
                (origin, particleEffect) -> {
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        double y = 0; // Default for flat rings

                        // Rotate the coordinates based on the specified axis
                        switch (rotationAxis != null ? rotationAxis.toUpperCase() : "") {
                            case "X" -> {
                                // Rotate around X-axis (vertical ring facing YZ-plane)
                                double tempY = y;
                                y = tempY * Math.cos(angle) - z * Math.sin(angle);
                                z = tempY * Math.sin(angle) + z * Math.cos(angle);
                            }
                            case "Y" -> {
                                // Default flat ring (rotation around Y is implicit in x/z calculation)
                            }
                            case "Z" -> {
                                // Rotate around Z-axis (vertical ring facing XY-plane)
                                double tempX = x;
                                x = tempX * Math.cos(angle) - y * Math.sin(angle);
                                y = tempX * Math.sin(angle) + y * Math.cos(angle);
                            }
                        }

                        Location particleLocation = origin.clone().add(x, y, z);
                        playParticleEffect(world, particleLocation, particleEffect);
                    }
                }
        ));
    }
    /**
     * Creates a static particle sphere and registers it with the ParticleManager.
     *
     * @param location  The center of the sphere.
     * @param radius    The radius of the sphere.
     * @param effect    The particle effect to use.
     * @param points    The number of particles per layer.
     * @param layers    The number of layers in the sphere.
     * @param duration  The duration of the effect in ticks.
     */
    public void createParticleSphere(Location location, double radius, ParticleEffect effect, int points, int layers, int duration) {
        World world = location.getWorld();
        if (world == null) return;

        // Register the effect
        particleManager.registerEffect(new ParticleManager.ActiveParticleEffect(
                location, effect, duration,
                (origin, particleEffect) -> {
                    for (int j = 0; j < layers; j++) {
                        double verticalAngle = Math.PI * j / layers;

                        for (int i = 0; i < points; i++) {
                            double horizontalAngle = 2 * Math.PI * i / points;
                            double x = origin.getX() + radius * Math.sin(verticalAngle) * Math.cos(horizontalAngle);
                            double y = origin.getY() + radius * Math.cos(verticalAngle);
                            double z = origin.getZ() + radius * Math.sin(verticalAngle) * Math.sin(horizontalAngle);

                            Location particleLocation = new Location(world, x, y, z);
                            playParticleEffect(world, particleLocation, particleEffect);
                        }
                    }
                }
        ));
    }

    /**
     * Creates a static particle box and registers it with the ParticleManager.
     *
     * @param center   The center of the box.
     * @param sizeX    The size of the box along the X-axis.
     * @param sizeY    The size of the box along the Y-axis.
     * @param sizeZ    The size of the box along the Z-axis.
     * @param effect   The particle effect to use.
     * @param density  The density of the particles on each edge (higher = more particles).
     * @param duration The duration of the effect in ticks.
     */
    public void createParticleBox(Location center, double sizeX, double sizeY, double sizeZ, ParticleEffect effect, int density, int duration) {
        World world = center.getWorld();
        if (world == null) return;

        // Register the effect
        particleManager.registerEffect(new ParticleManager.ActiveParticleEffect(
                center, effect, duration,
                (origin, particleEffect) -> {
                    for (double x = -sizeX / 2; x <= sizeX / 2; x += sizeX / density) {
                        for (double y = -sizeY / 2; y <= sizeY / 2; y += sizeY / density) {
                            for (double z = -sizeZ / 2; z <= sizeZ / 2; z += sizeZ / density) {
                                Location particleLocation = origin.clone().add(x, y, z);
                                playParticleEffect(world, particleLocation, particleEffect);
                            }
                        }
                    }
                }
        ));
    }

    /**
     * Creates a static particle line and registers it with the ParticleManager.
     *
     * @param start    The starting location.
     * @param end      The ending location.
     * @param effect   The particle effect to use.
     * @param points   The number of particles along the line.
     * @param duration The duration of the effect in ticks.
     */
    public void createParticleLine(Location start, Location end, ParticleEffect effect, int points, int duration) {
        World world = start.getWorld();
        if (world == null || !world.equals(end.getWorld())) return;

        // Register the effect
        particleManager.registerEffect(new ParticleManager.ActiveParticleEffect(
                start, effect, duration,
                (origin, particleEffect) -> {
                    for (int i = 0; i <= points; i++) {
                        double t = (double) i / points;
                        double x = start.getX() + (end.getX() - start.getX()) * t;
                        double y = start.getY() + (end.getY() - start.getY()) * t;
                        double z = start.getZ() + (end.getZ() - start.getZ()) * t;

                        Location particleLocation = new Location(world, x, y, z);
                        playParticleEffect(world, particleLocation, particleEffect);
                    }
                }
        ));
    }

    /**
     * Utility method to play a particle effect at a specific location.
     */
    public void playParticleEffect(World world, Location location, ParticleEffect effect) {
        world.spawnParticle(
                effect.getParticleType(),
                location,
                effect.getCount(),
                effect.getOffsetX(),
                effect.getOffsetY(),
                effect.getOffsetZ(),
                effect.getSpeed(),
                effect.getData()
        );
    }
}

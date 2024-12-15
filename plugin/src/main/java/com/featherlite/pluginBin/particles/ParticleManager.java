package com.featherlite.pluginBin.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ParticleManager {

    private final JavaPlugin plugin;
    private final List<ActiveParticleEffect> activeEffects = new CopyOnWriteArrayList<>();

    public ParticleManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startParticleClock();
    }

    /**
     * Starts the centralized particle clock.
     */
    private void startParticleClock() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Iterate through all active effects
            Iterator<ActiveParticleEffect> iterator = activeEffects.iterator();
            while (iterator.hasNext()) {
                ActiveParticleEffect effect = iterator.next();

                // Update and play the effect
                if (!effect.updateAndPlay()) {
                    iterator.remove(); // Remove the effect if it has expired
                }
            }
        }, 0L, 1L); // Runs every tick
    }

    /**
     * Registers a new particle effect to be managed.
     *
     * @param effect The effect to register.
     */
    public void registerEffect(ActiveParticleEffect effect) {
        activeEffects.add(effect);
    }

    /**
     * Clears all active particle effects.
     */
    public void clearEffects() {
        activeEffects.clear();
    }

    // Nested class for managing individual particle effects
    public static class ActiveParticleEffect {
        private final Location origin;
        private final ParticleEffectCreator.ParticleEffect particleEffect;
        private final int duration;
        private final EffectUpdater updater;

        private int ticksElapsed = 0;

        /**
         * Constructor for a dynamic particle effect.
         *
         * @param origin        The initial location of the effect.
         * @param particleEffect The particle effect to play.
         * @param duration      The duration of the effect in ticks.
         * @param updater       The update logic for the effect.
         */
        public ActiveParticleEffect(Location origin, ParticleEffectCreator.ParticleEffect particleEffect, int duration, EffectUpdater updater) {
            this.origin = origin;
            this.particleEffect = particleEffect;
            this.duration = duration;
            this.updater = updater;
        }

        /**
         * Updates and plays the effect.
         *
         * @return true if the effect is still active, false if it should be removed.
         */
        public boolean updateAndPlay() {
            if (ticksElapsed++ >= duration) {
                return false; // Effect has expired
            }

            // Update the location and play the effect
            updater.update(origin, particleEffect);
            return true;
        }
    }

    /**
     * Functional interface for updating particle effects dynamically.
     */
    @FunctionalInterface
    public interface EffectUpdater {
        void update(Location origin, ParticleEffectCreator.ParticleEffect effect);
    }
}

package com.featherlite.pluginBin.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import org.bukkit.entity.Player;
//test
import org.bukkit.util.Vector;

import com.featherlite.pluginBin.displays.DisplayPiece;
import com.featherlite.pluginBin.displays.DisplayPieceManager;
import com.featherlite.pluginBin.particles.ParticleEffectCreator;
import com.featherlite.pluginBin.particles.ParticleManager;
import com.featherlite.pluginBin.projectiles.ProjectileManager;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import org.bukkit.Color;

public class InternalAbilities {

    private final ProjectileManager projectileManager;
    private final ParticleManager particleManager;
    private final DisplayPieceManager displayPieceManager;
    private final boolean isDebuggerOn;

    public InternalAbilities(ProjectileManager projectileManager, ParticleManager particleManager, DisplayPieceManager displayPieceManager, boolean isDebuggerOn) {
        this.projectileManager = projectileManager;
        this.particleManager = particleManager;
        this.displayPieceManager = displayPieceManager;
        this.isDebuggerOn = isDebuggerOn;
        
    }


    /**
     * Makes the player leap forward with particle effects.
     */
    public void leap(Player player, Map<String, String> params) {
        double strength = Double.parseDouble(params.getOrDefault("leap_strength", "1.5"));
    
        // Get the direction the player is looking and apply thrust in that direction
        Vector direction = player.getLocation().getDirection().normalize().multiply(strength);
        player.setVelocity(direction);
    
        // Play sound and particles for added effect
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.0f);
        player.getWorld().spawnParticle(Particle.SONIC_BOOM, player.getLocation(), 10, 0.1, 0.1, 0.1, 0.1);
        
        if (isDebuggerOn) {player.sendMessage("§aYou thrust forward with strength " + strength + "!");}
    }

    /**
     * Strikes lightning at the player's target location.
     */
    public void strikeLightning(Player player, Map<String, String> params) {
        int range = Integer.parseInt(params.getOrDefault("lightning_range", "10"));
        Location targetLocation = player.getTargetBlock(null, range).getLocation();

        World world = player.getWorld();
        world.strikeLightning(targetLocation);
        world.spawnParticle(Particle.CRIT, targetLocation, 20, 0.5, 1.0, 0.5, 0.1);

        if (isDebuggerOn) {player.sendMessage("§eYou called down lightning within " + range + " blocks!");}
    }

    public void magicMissile(Player player, Map<String, String> params) {
        // Parse parameters
        int damage = Integer.parseInt(params.getOrDefault("damage", "5"));
        int range = Integer.parseInt(params.getOrDefault("range", "20"));
        double speed = Double.parseDouble(params.getOrDefault("speed", "2.0"));
        String colorName = params.getOrDefault("color", "purple").toLowerCase();

        // Define particle color and display block based on colorName
        Color particleColor;
        ItemStack displayMaterial;

        switch (colorName) {
            case "red" -> {
                particleColor = Color.RED;
                displayMaterial = new ItemStack(Material.RED_STAINED_GLASS);
            }
            case "orange" -> {
                particleColor = Color.ORANGE;
                displayMaterial = new ItemStack(Material.ORANGE_STAINED_GLASS);
            }
            case "yellow" -> {
                particleColor = Color.YELLOW;
                displayMaterial = new ItemStack(Material.YELLOW_STAINED_GLASS);
            }
            case "lime" -> {
                particleColor = Color.LIME;
                displayMaterial = new ItemStack(Material.LIME_STAINED_GLASS);
            }
            case "green" -> {
                particleColor = Color.GREEN;
                displayMaterial = new ItemStack(Material.GREEN_STAINED_GLASS);
            }
            case "cyan" -> {
                particleColor = Color.AQUA;
                displayMaterial = new ItemStack(Material.CYAN_STAINED_GLASS);
            }
            case "light_blue" -> {
                particleColor = Color.AQUA;
                displayMaterial = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS);
            }
            case "blue" -> {
                particleColor = Color.BLUE;
                displayMaterial = new ItemStack(Material.BLUE_STAINED_GLASS);
            }
            case "purple" -> {
                particleColor = Color.PURPLE;
                displayMaterial = new ItemStack(Material.PURPLE_STAINED_GLASS);
            }
            case "magenta" -> {
                particleColor = Color.FUCHSIA;
                displayMaterial = new ItemStack(Material.MAGENTA_STAINED_GLASS);
            }
            case "pink" -> {
                particleColor = Color.fromRGB(255, 182, 193); // Light pink
                displayMaterial = new ItemStack(Material.PINK_STAINED_GLASS);
            }
            default -> {
                particleColor = Color.PURPLE; // Default color
                displayMaterial = new ItemStack(Material.PURPLE_STAINED_GLASS);
            }
        }

        Location spawnLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(2.0));
        

        // Use the passed DisplayPieceManager to create a DisplayPiece
        DisplayPiece displayPiece = displayPieceManager.createItemDisplay(
                "magic_missile_" + System.currentTimeMillis(),
                player.getWorld(),
                player.getLocation(),
                displayMaterial,
                false
        );

        displayPiece.scale(0.5f);

        // Launch the projectile
        projectileManager.launchProjectile(
                player,
                spawnLocation, // Start location (eye height)
                player.getLocation().getDirection(), // Direction the player is looking
                speed,                               // Speed
                range,                               // Lifetime in ticks
                displayPiece,                        // Display entity
                hitResult -> {
                    // Collision logic
                    if (hitResult.hasHitEntity()) {
                        Entity entity = hitResult.getHitEntity();
                        
                        // Check if the entity is a LivingEntity before applying damage
                        if (entity instanceof org.bukkit.entity.LivingEntity livingEntity) {
                            livingEntity.damage(damage); // Deal 5 damage to the entity
                            if (isDebuggerOn) {player.sendMessage("§aMagic Missile hit: " + livingEntity.getName());}
                        } else {
                           if (isDebuggerOn) {player.sendMessage("§eHit an entity, but it can't take damage: " + entity.getName());}
                        }
                    } else if (hitResult.hasHitBlock()) {
                       if (isDebuggerOn) {player.sendMessage("§aMagic Missile hit a block: " + hitResult.getHitBlock().getType());}
                    }
                },
                projectile -> {
                    // Particle trail logic
                    Location loc = projectile.getLocation();
                    ParticleEffectCreator creator = new ParticleEffectCreator(particleManager);
                    
                    ParticleEffectCreator.ParticleEffect dustEffect = ParticleEffectCreator.createEffect(
                            Particle.DUST, // Use DUST for RGB colored particles
                            1,             // Particle count
                            0, 0, 0,       // Offset values
                            0,             // Speed
                            new Particle.DustOptions(particleColor, 1.0F) // Color and size
                    );
                    
                    // Spawn a particle at the projectile's current location
                    creator.playParticleEffect(loc.getWorld(), loc, dustEffect);
                }
        );
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 0.6f);

    }

}

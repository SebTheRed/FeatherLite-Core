package com.featherlite.pluginBin.items;

import org.bukkit.Location;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import org.bukkit.entity.Player;
//test
import org.bukkit.util.Vector;
import java.util.Map;


public class InternalAbilities {


    public InternalAbilities() {

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
        
        player.sendMessage("§aYou thrust forward with strength " + strength + "!");
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

        player.sendMessage("§eYou called down lightning within " + range + " blocks!");
    }


}

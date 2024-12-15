package com.featherlite.pluginBin.items;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    // Check if the player has the ability on cooldown
    public boolean isOnCooldown(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return false;

        Long cooldownEnd = playerCooldowns.get(abilityName);
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }

    // Set cooldown for an ability
    public void setCooldown(Player player, String abilityName, int cooldownInSeconds) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                 .put(abilityName, System.currentTimeMillis() + (cooldownInSeconds * 1000L));
    }

    // Get time left on cooldown for a player’s ability
    public int getCooldownTimeLeft(Player player, String abilityName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long cooldownEnd = playerCooldowns.get(abilityName);
        if (cooldownEnd == null) return 0;

        return (int) ((cooldownEnd - System.currentTimeMillis()) / 1000);
    }
}

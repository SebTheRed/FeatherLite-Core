package com.featherlite.pluginBin.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ItemListeners implements Listener {
    private final JavaPlugin plugin;
    private final AbilityRegistry abilityRegistry;
    private final CooldownManager cooldownManager; // Add CooldownManager
    private final Map<String, Map<UUID, Long>> debounceMap = new HashMap<>(); // Map for debouncing (trigger -> (playerUUID -> timestamp))


    public ItemListeners(AbilityRegistry abilityRegistry, CooldownManager cooldownManager, JavaPlugin plugin) {
        this.plugin = plugin;
        this.abilityRegistry = abilityRegistry;
        this.cooldownManager = cooldownManager; // Initialize CooldownManager
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startPassiveTickAbility();
    }

    // Helper method to invoke ability based on item metadata and trigger
    private void invokeAbility(Player player, ItemStack item, String trigger) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Debounce check: only allow invocation if it's not within 1 tick of the last trigger
        if (isDebounced(player, trigger)) return;
        setDebounce(player, trigger);
        // Look up ability for the trigger
        NamespacedKey abilityKey = new NamespacedKey(plugin, "ability_" + trigger);
        if (!meta.getPersistentDataContainer().has(abilityKey, PersistentDataType.STRING)) return;

        // Retrieve the method name and parameters
        String methodName = meta.getPersistentDataContainer().get(abilityKey, PersistentDataType.STRING);
        Map<String, String> params = new HashMap<>();

        // Collect parameters for this specific trigger
        meta.getPersistentDataContainer().getKeys().forEach(key -> {
            if (key.getKey().startsWith("ability_" + trigger + "_")) {
                
                String paramName = key.getKey().substring(("ability_" + trigger + "_").length());
                paramName = paramName.substring(0, 1).toLowerCase() + paramName.substring(1); 

                String paramValue = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                params.put(paramName, paramValue);
            }
        });

        // Get cooldown (if any) from parameters
        int cooldown = Integer.parseInt(params.getOrDefault("cooldown", "0"));
        String abilityTitle = params.getOrDefault("title", methodName); // Use title as cooldown identifier if available

        // Check if the ability is on cooldown
        if (cooldownManager.isOnCooldown(player, abilityTitle)) {
            int timeLeft = cooldownManager.getCooldownTimeLeft(player, abilityTitle);
            player.sendMessage("§cAbility " + abilityTitle + " is on cooldown for " + timeLeft + " seconds.");
            return;
        }

        // Start the cooldown if there is one
        if (cooldown > 0) {
            cooldownManager.setCooldown(player, abilityTitle, cooldown);
        }

        // Invoke the ability using AbilityRegistry
        abilityRegistry.invokeAbility(player, methodName, params);
    }
    


        // Debounce check for player and trigger
        private boolean isDebounced(Player player, String trigger) {
            UUID playerId = player.getUniqueId();
            return debounceMap.getOrDefault(trigger, new HashMap<>()).containsKey(playerId);
        }
    
        // Set debounce for player and trigger, with a 1-tick delay
        private void setDebounce(Player player, String trigger) {
            UUID playerId = player.getUniqueId();
            debounceMap.computeIfAbsent(trigger, k -> new HashMap<>()).put(playerId, System.currentTimeMillis());
    
            // Remove the debounce entry after 1 tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    Map<UUID, Long> triggerMap = debounceMap.get(trigger);
                    if (triggerMap != null) {
                        triggerMap.remove(playerId);
                    }
                }
            }.runTaskLater(plugin, 1); // 1-tick debounce
        }


    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                invokeAbility(player, item, "shift_r_click");
            } else {
                invokeAbility(player, item, "r_click");
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (player.isSneaking()) {
                invokeAbility(player, item, "shift_l_click");
            } else {
                invokeAbility(player, item, "l_click");
            }
        }
    }

    @EventHandler
    public void onPlayerAttackEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null) {
                invokeAbility(player, item, "on_attack");
            }
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null) {
                invokeAbility(player, item, "on_damaged");
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
    
        // Skip if item is a potion (as these are handled separately)
        if (item.getType().toString().contains("potion")) {
            return;
        }
    
        if (item.hasItemMeta()) {
            // Check for effect data on the item
            String effectName = item.getItemMeta().getPersistentDataContainer()
                    .get(new NamespacedKey(plugin, "effect"), PersistentDataType.STRING);
            int duration = item.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "duration"), PersistentDataType.INTEGER,3);
            int amplifier = item.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "amplifier"), PersistentDataType.INTEGER, 1); // Default 1 second
    
            if (effectName != null) {
                PotionEffectType effectType = Registry.EFFECT.get(NamespacedKey.minecraft(effectName.toLowerCase()));
                if (effectType != null) {
                    player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                }
            }
        }
    }
    




    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item != null) {
            invokeAbility(player, item, "on_block_place");
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();
            if (bow != null) {
                invokeAbility(player, bow, "on_bow_shoot");
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            if (player != null) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null) {
                    invokeAbility(player, item, "on_throw");
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            if (player != null) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null) {
                    invokeAbility(player, item, "on_bow_attack");
                }
            }
        }
    }

    private void startPassiveTickAbility() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item != null) {
                        invokeAbility(player, item, "on_tick");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second (20 ticks)
    }
}

package com.featherlite.pluginBin.items;

import org.bukkit.NamespacedKey;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BowAttributesHandler {
    private static final Map<UUID, Long> bowDrawStartTimes = new HashMap<>();

    public static void handleArrowVelocity(EntityShootBowEvent event, JavaPlugin plugin) {
        Player player = (Player) event.getEntity();
        ItemStack bow = event.getBow();

        if (bow != null) {
            double arrowVelocity = bow.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(new NamespacedKey(plugin, "arrowVelocity"), PersistentDataType.DOUBLE, 1.0);

            if (event.getProjectile() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getProjectile();
                arrow.setVelocity(arrow.getVelocity().multiply(arrowVelocity));
            }
        }
    }

}

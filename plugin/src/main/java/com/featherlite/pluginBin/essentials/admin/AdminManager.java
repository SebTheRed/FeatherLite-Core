package com.featherlite.pluginBin.essentials.admin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;

public class AdminManager {
    private final Set<UUID> godModePlayers = new HashSet<>();


    public boolean enchantItem(Player player, String enchantmentName, int level) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not holding an item.");
            return false;
        }

        Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
        if (enchantment == null) {
            player.sendMessage(ChatColor.RED + "Invalid enchantment.");
            return false;
        }

        item.addUnsafeEnchantment(enchantment, level);
        player.sendMessage(ChatColor.GREEN + "Item successfully enchanted!");
        return true;
    }

    public void giveExp(Player player, int amount) {
        player.giveExp(amount);
        player.sendMessage(ChatColor.GREEN + "Gave " + amount + " experience points.");
    }

    public void setExp(Player player, int level) {
        player.setLevel(level);
        player.sendMessage(ChatColor.GREEN + "Set experience level to " + level + ".");
    }

    // public void viewExp(Player player) {
    //     player.sendMessage(ChatColor.YELLOW + "Your current XP: " + player.getExp() + ", Level: " + player.getLevel());
    // }

    public boolean giveItem(Player sender, Player target, Material material, int amount) {

        target.getInventory().addItem(new ItemStack(material, amount));
        sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " of " + material.name() + " to " + target.getName() + ".");
        return true;
    }

    public void killPlayer(Player target) {
        target.setHealth(0);
    }

    public void sudoPlayer(Player sender, Player target, String command) {
        target.performCommand(command);
        sender.sendMessage(ChatColor.GREEN + "Forced " + target.getName() + " to execute: " + command);
    }

    public void setWeather(World world, String weatherType) {
        switch (weatherType.toLowerCase()) {
            case "clear":
                world.setStorm(false);
                world.setThundering(false);
                break;
            case "rain":
                world.setStorm(true);
                world.setThundering(false);
                break;
            case "thunder":
                world.setStorm(true);
                world.setThundering(true);
                break;
        }
    }

    public void setTime(World world, String timeArg) {
        long time;
    
        switch (timeArg.toLowerCase()) {
            case "morning":
                time = 0; // Sunrise (start of the day)
                break;
            case "noon":
                time = 6000; // Midday
                break;
            case "night":
                time = 13000; // Sunset
                break;
            case "midnight":
                time = 18000;
                break;
            default:
                try {
                    // Attempt to parse as a tick value
                    time = Long.parseLong(timeArg);
                    if (time < 0 || time > 24000) {
                        throw new NumberFormatException("Invalid tick range"); // Custom exception for bad range
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid time. Use 'morning', 'noon', 'night', or a tick value (0-24000).");
                }
        }
    
        world.setTime(time);
    }

    public boolean toggleGodMode(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (godModePlayers.contains(playerUUID)) {
            godModePlayers.remove(playerUUID);
            player.setInvulnerable(false);
            // player.sendMessage(ChatColor.RED + "God Mode disabled.");
            return false; // Disabled
        } else {
            godModePlayers.add(playerUUID);
            player.setInvulnerable(true);
            // player.sendMessage(ChatColor.GREEN + "God Mode enabled.");
            return true; // Enabled
        }
    }

    public boolean isGodMode(Player player) {
        return godModePlayers.contains(player.getUniqueId());
    }






    public void killAll(Player player, String targetType, String scope) {
        World world = player.getWorld();
        int count = 0;
    
        switch (targetType.toLowerCase()) {
            case "monsters":
                count = world.getEntitiesByClass(org.bukkit.entity.Monster.class).size();
                world.getEntitiesByClass(org.bukkit.entity.Monster.class).forEach(entity -> {
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                    }
                });
                break;
            case "entities":
                for (var entity : world.getEntities()) {
                    if (entity instanceof Player) {
                        // Skip players since they can't be removed this way
                        continue;
                    }
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                        count++;
                    }
                }
                break;
            case "boats":
                count = world.getEntitiesByClass(org.bukkit.entity.Boat.class).size();
                world.getEntitiesByClass(org.bukkit.entity.Boat.class).forEach(entity -> {
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                    }
                });
                break;
            case "minecarts":
                count = world.getEntitiesByClass(org.bukkit.entity.Minecart.class).size();
                world.getEntitiesByClass(org.bukkit.entity.Minecart.class).forEach(entity -> {
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                    }
                });
                break;
            case "players":
                Bukkit.getOnlinePlayers().forEach(target -> {
                    if (!target.equals(player) && (scope.equalsIgnoreCase("world") || player.getLocation().distance(target.getLocation()) <= parseRadius(scope))) {
                        target.setHealth(0); // Kills the player
                    }
                });
                count = Bukkit.getOnlinePlayers().size() - 1; // Exclude the sender
                break;
            case "drops":
                count = world.getEntitiesByClass(org.bukkit.entity.Item.class).size();
                world.getEntitiesByClass(org.bukkit.entity.Item.class).forEach(entity -> {
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                    }
                });
                break;
            case "arrows":
                count = world.getEntitiesByClass(org.bukkit.entity.Arrow.class).size();
                world.getEntitiesByClass(org.bukkit.entity.Arrow.class).forEach(entity -> {
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                    }
                });
                break;
            case "mobs":
                for (var entity : world.getEntitiesByClass(org.bukkit.entity.LivingEntity.class)) {
                    if (entity instanceof Player) {
                        // Skip players when targeting mobs
                        continue;
                    }
                    if (scope.equalsIgnoreCase("world") || player.getLocation().distance(entity.getLocation()) <= parseRadius(scope)) {
                        entity.remove();
                        count++;
                    }
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid type. Use /killall <monsters|entities|boats|minecarts|players|drops|arrows|mobs> [radius|world].");
                return;
        }
    
        player.sendMessage(ChatColor.GREEN + "Removed " + count + " " + targetType + ".");
    }
    
    // Helper method to parse radius
    private double parseRadius(String scope) {
        try {
            return Double.parseDouble(scope);
        } catch (NumberFormatException e) {
            return 0; // If invalid radius, default to 0
        }
    }
    




    
}

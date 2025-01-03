package com.featherlite.pluginBin.essentials.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.meta.Damageable;

import com.featherlite.pluginBin.essentials.PlayerDataManager;

public class UtilManager {

    private final Map<UUID, Boolean> afkPlayers = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();



    public boolean toggleFlight(Player player) {
        boolean canFly = !player.getAllowFlight();
        player.setAllowFlight(canFly);
        player.sendMessage(ChatColor.GREEN + "Flight mode " + (canFly ? "enabled." : "disabled."));
        return true;
    }

    public boolean setWalkSpeed(Player player, String speedArg) {
        try {
            float speed = Float.parseFloat(speedArg);
            if (speed < 0 || speed > 1) {
                player.sendMessage(ChatColor.RED + "Speed must be between 0 and 1.");
                return true;
            }
            player.setWalkSpeed(speed);
            player.sendMessage(ChatColor.GREEN + "Walk speed set to " + speed + ".");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid speed value.");
        }
        return true;
    }

    public boolean setFlySpeed(Player player, String speedArg) {
        try {
            float speed = Float.parseFloat(speedArg);
            if (speed < 0 || speed > 1) {
                player.sendMessage(ChatColor.RED + "Fly speed must be between 0 and 1.");
                return true;
            }
            player.setFlySpeed(speed);
            player.sendMessage(ChatColor.GREEN + "Fly speed set to " + speed + ".");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid fly speed value.");
        }
        return true;
    }

    public boolean setGameMode(Player player, String mode) {
        GameMode gameMode;
        switch (mode.toLowerCase()) {
            case "survival":
                gameMode = GameMode.SURVIVAL;
                break;
            case "creative":
                gameMode = GameMode.CREATIVE;
                break;
            case "spectator":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid game mode.");
                return true;
        }
        player.setGameMode(gameMode);
        player.sendMessage(ChatColor.GREEN + "Game mode set to " + mode + ".");
        return true;
    }

    public boolean healPlayer(Player player, String[] args) {
        // Read cooldown from config
        int cooldown = Bukkit.getPluginManager().getPlugin("FeatherLite-Core")
                              .getConfig().getInt("command-cooldowns.heal", 60);
    
        // Check if the player is on cooldown
        if (isOnCooldown(player, "heal", cooldown)) return true;
    
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
        target.setHealth(maxHealth);
        player.sendMessage(ChatColor.GREEN + target.getName() + " has been healed.");
        return true;
    }
    

    public boolean feedPlayer(Player player, String[] args) {
        // Read cooldown from config
        int cooldown = Bukkit.getPluginManager().getPlugin("FeatherLite-Core")
                              .getConfig().getInt("command-cooldowns.feed", 60);
    
        // Check if the player is on cooldown
        if (isOnCooldown(player, "feed", cooldown)) return true;
    
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
    
        // Set food level and max out saturation
        target.setFoodLevel(20); // Max food level
        target.setSaturation(20.0f); // Max saturation level
        player.sendMessage(ChatColor.GREEN + target.getName() + " has been fed and their saturation maxed out.");
        return true;
    }
    
    

    public boolean restPlayer(Player player, String[] args) {
        // Read cooldown from config
        int cooldown = Bukkit.getPluginManager().getPlugin("FeatherLite-Core")
                              .getConfig().getInt("command-cooldowns.rest", 60);
    
        // Check if the player is on cooldown
        if (isOnCooldown(player, "rest", cooldown)) return true;
    
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
        target.setHealth(maxHealth);
        target.setFoodLevel(20);
        player.sendMessage(ChatColor.GREEN + target.getName() + " is now rested.");
        return true;
    }
    

    public boolean repairItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not holding an item.");
            return false;
        }
    
        if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            item.setItemMeta(damageable);
            player.sendMessage(ChatColor.GREEN + "Item successfully repaired!");
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "This item cannot be repaired.");
            return false;
        }
    }
    


    public boolean markAFK(Player player) {
        player.sendMessage(ChatColor.YELLOW + "You are now marked as AFK.");
        Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " is now AFK.");
        return true;
    }

    public boolean toggleAFK(Player player) {
        // No cooldown for AFK toggle as per the current design, but could be added later
        boolean isAFK = afkPlayers.getOrDefault(player.getUniqueId(), false);
    
        if (isAFK) {
            afkPlayers.remove(player.getUniqueId());
            Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " is no longer AFK.");
        } else {
            afkPlayers.put(player.getUniqueId(), true);
            Bukkit.broadcastMessage(ChatColor.GRAY + player.getName() + " is now AFK.");
        }
        return true;
    }

    public boolean openEnderChest(Player player) {
        player.openInventory(player.getEnderChest());
        player.sendMessage(ChatColor.GREEN + "Opened your Ender Chest.");
        return true;
    }

    public boolean openTrash(Player player) {
        player.openInventory(Bukkit.createInventory(null, 27, ChatColor.RED + "Trash Bin"));
        player.sendMessage(ChatColor.GREEN + "Opened trash bin.");
        return true;
    }

    public boolean teleportToTop(Player player) {
        Location location = player.getLocation();
        Location highest = player.getWorld().getHighestBlockAt(location).getLocation();
        highest.setY(highest.getY() + 1); // Teleport just above the block
        player.teleport(highest);
        player.sendMessage(ChatColor.GREEN + "Teleported to the top block.");
        return true;
    }

    public boolean wearHat(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You cannot use air as a hat.");
            return true;
        }
        player.getInventory().setHelmet(itemInHand);
        player.getInventory().setItemInMainHand(helmet);
        player.sendMessage(ChatColor.GREEN + "You're now wearing the item as a hat!");
        return true;
    }

    public boolean setNickname(Player player, String[] args, PlayerDataManager playerDataManager) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /nick <new nickname>");
            return true;
        }
    
        // Create and format the nickname
        String nickname = String.join(" ", args);
        String formattedNickname = ChatColor.translateAlternateColorCodes('&', nickname);
    
        // Apply the nickname to the player
        player.setDisplayName(formattedNickname);
        player.setPlayerListName(formattedNickname);
        player.sendMessage(ChatColor.GREEN + "Nickname set to " + formattedNickname);
    
        // Save the nickname to the player's data file as plain text
        FileConfiguration playerData = playerDataManager.getPlayerData(player);
        playerData.set("nickname", nickname); // Save raw nickname (without color codes)
        playerDataManager.savePlayerData(player, playerData);
    
        return true;
    }

    public boolean getRealName(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /realname <nickname>");
            return true;
        }
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getDisplayName().equalsIgnoreCase(args[0])) {
                player.sendMessage(ChatColor.GREEN + args[0] + "'s real name is " + onlinePlayer.getName());
                return true;
            }
        }
        player.sendMessage(ChatColor.RED + "No player with that nickname was found.");
        return true;
    }

    public boolean listPlayers(Player player) {
        String playerList = Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No players online.");
        player.sendMessage(ChatColor.GREEN + "Online players: " + playerList);
        return true;
    }

    public boolean nearPlayers(Player player, String[] args) {
        double radius = args.length > 0 ? Double.parseDouble(args[0]) : 50; // Default radius is 50
        List<String> nearbyPlayers = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target != player && player.getLocation().distance(target.getLocation()) <= radius) {
                nearbyPlayers.add(target.getName());
            }
        }
        player.sendMessage(ChatColor.GREEN + "Nearby players: " + String.join(", ", nearbyPlayers));
        return true;
    }

    public boolean getPlayerPosition(Player player, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        Location loc = target.getLocation();
        player.sendMessage(ChatColor.GREEN + target.getName() + "'s position: X=" + loc.getBlockX() + ", Y=" + loc.getBlockY() + ", Z=" + loc.getBlockZ());
        return true;
    }

    public boolean pingPlayer(Player player) {
        player.sendMessage(ChatColor.GREEN + "Your ping is " + player.getPing() + "ms.");
        return true;
    }

    public boolean seenPlayer(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /seen <playername>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        long lastSeen = target.getLastPlayed();
        player.sendMessage(ChatColor.GREEN + target.getName() + " was last seen on " + new java.util.Date(lastSeen).toString());
        return true;
    }

    public boolean openWorkbench(Player player) {
        player.openWorkbench(null, true);
        player.sendMessage(ChatColor.GREEN + "Opened a crafting table.");
        return true;
    }

    public boolean openAnvil(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.ANVIL));
        player.sendMessage(ChatColor.GREEN + "Opened an anvil.");
        return true;
    }

    public boolean openCartographyTable(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.CARTOGRAPHY));
        player.sendMessage(ChatColor.GREEN + "Opened a cartography table.");
        return true;
    }

    public boolean openGrindstone(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.GRINDSTONE));
        player.sendMessage(ChatColor.GREEN + "Opened a grindstone.");
        return true;
    }

    public boolean openLoom(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.LOOM));
        player.sendMessage(ChatColor.GREEN + "Opened a loom.");
        return true;
    }

    public boolean openSmithingTable(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.SMITHING));
        player.sendMessage(ChatColor.GREEN + "Opened a smithing table.");
        return true;
    }

    public boolean openStonecutter(Player player) {
        player.openInventory(Bukkit.createInventory(null, InventoryType.STONECUTTER));
        player.sendMessage(ChatColor.GREEN + "Opened a stonecutter.");
        return true;
    }

    public boolean setPlayerTime(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /ptime <morning|noon|night|ticks>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "morning":
                player.setPlayerTime(0, false); // Set to sunrise
                player.sendMessage(ChatColor.GREEN + "Your personal time is set to morning.");
                break;
            case "noon":
                player.setPlayerTime(6000, false); // Set to noon
                player.sendMessage(ChatColor.GREEN + "Your personal time is set to noon.");
                break;
            case "night":
                player.setPlayerTime(18000, false); // Set to night
                player.sendMessage(ChatColor.GREEN + "Your personal time is set to night.");
                break;
            default:
                try {
                    long ticks = Long.parseLong(args[0]);
                    player.setPlayerTime(ticks, false);
                    player.sendMessage(ChatColor.GREEN + "Your personal time is set to " + ticks + " ticks.");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Invalid time format. Use a number or morning/noon/night.");
                }
                break;
        }
        return true;
    }

    public boolean setPlayerWeather(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /pweather <clear|storm|thunder>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "clear":
                player.setPlayerWeather(WeatherType.CLEAR);
                player.sendMessage(ChatColor.GREEN + "Your personal weather is set to clear.");
                break;
            case "storm":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                player.sendMessage(ChatColor.GREEN + "Your personal weather is set to storm.");
                break;
            case "thunder":
                player.setPlayerWeather(WeatherType.DOWNFALL);
                player.sendMessage(ChatColor.GREEN + "Your personal weather is set to thunder.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Invalid weather type. Use clear, storm, or thunder.");
                break;
        }
        return true;
    }


    public boolean isOnCooldown(Player player, String command, int cooldownSeconds) {
        if (player.isOp()) return false; // Skip cooldown for OPs
    
        Map<String, Long> playerCooldowns = cooldowns.getOrDefault(player.getUniqueId(), new HashMap<>());
        long currentTime = System.currentTimeMillis();
        long cooldownEndTime = playerCooldowns.getOrDefault(command, 0L);
    
        if (currentTime < cooldownEndTime) {
            long remainingTime = (cooldownEndTime - currentTime) / 1000;
            player.sendMessage(ChatColor.RED + "You must wait " + remainingTime + " seconds to use /" + command + " again.");
            return true;
        }
    
        // Update cooldown time
        playerCooldowns.put(command, currentTime + (cooldownSeconds * 1000));
        cooldowns.put(player.getUniqueId(), playerCooldowns);
        return false;
    }


}
